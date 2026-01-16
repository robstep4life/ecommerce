package robben.ecommerce.service;

import robben.ecommerce.dto.CartDtos;
import robben.ecommerce.entity.*;
import robben.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final CurrentUserService currentUserService;

    public CartService(CartRepository carts,
                       CartItemRepository cartItems,
                       ProductRepository products,
                       CurrentUserService currentUserService) {
        this.carts = carts;
        this.cartItems = cartItems;
        this.products = products;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CartDtos.CartResponse getMyCart() {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseGet(() -> carts.save(new Cart(user)));
        return toResponse(cart);
    }

    @Transactional
    public CartDtos.CartResponse addItem(CartDtos.AddItemRequest req) {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseGet(() -> carts.save(new Cart(user)));

        Product p = products.findById(req.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!p.isActive()) throw new IllegalArgumentException("Product not found");

        int addQty = req.quantity();
        if (addQty <= 0) throw new IllegalArgumentException("Quantity must be >= 1");

        CartItem item = cartItems.findByCartIdAndProductId(cart.getId(), p.getId()).orElse(null);
        int newQty = (item == null ? 0 : item.getQuantity()) + addQty;

        if (p.getInventory() < newQty) throw new IllegalArgumentException("Not enough inventory");

        if (item == null) {
            item = new CartItem(cart, p, newQty, p.getPrice());
        } else {
            item.setQuantity(newQty);
            item.setUnitPrice(p.getPrice());
        }

        cartItems.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartDtos.CartResponse updateItem(Long itemId, CartDtos.UpdateItemRequest req) {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        CartItem item = cartItems.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (!item.getCart().getId().equals(cart.getId())) throw new IllegalArgumentException("Item not found");

        int qty = req.quantity();
        if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0");

        Product p = item.getProduct();

        if (qty == 0) {
            cartItems.delete(item);
            return toResponse(cart);
        }

        if (p.getInventory() < qty) throw new IllegalArgumentException("Not enough inventory");

        item.setQuantity(qty);
        item.setUnitPrice(p.getPrice());
        cartItems.save(item);

        return toResponse(cart);
    }

    @Transactional
    public CartDtos.CartResponse removeItem(Long itemId) {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        CartItem item = cartItems.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (!item.getCart().getId().equals(cart.getId())) throw new IllegalArgumentException("Item not found");

        cartItems.delete(item);
        return toResponse(cart);
    }

    @Transactional
    public CartDtos.CartResponse clear() {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        List<CartItem> items = cartItems.findByCartId(cart.getId());
        cartItems.deleteAll(items);

        return toResponse(cart);
    }

    private CartDtos.CartResponse toResponse(Cart cart) {
        List<CartItem> items = cartItems.findByCartId(cart.getId());

        List<CartDtos.CartItemResponse> itemResponses = items.stream().map(ci -> {
            BigDecimal line = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartDtos.CartItemResponse(
                    ci.getId(),
                    ci.getProduct().getId(),
                    ci.getProduct().getSku(),
                    ci.getProduct().getName(),
                    ci.getUnitPrice(),
                    ci.getQuantity(),
                    line
            );
        }).toList();

        BigDecimal total = itemResponses.stream()
                .map(CartDtos.CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDtos.CartResponse(cart.getId(), itemResponses, total);
    }
}
