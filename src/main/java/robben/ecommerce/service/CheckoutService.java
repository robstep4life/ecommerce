package robben.ecommerce.service;

import robben.ecommerce.dto.OrderDtos;
import robben.ecommerce.entity.*;
import robben.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutService {

    private final CurrentUserService currentUserService;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final OrderRepository orders;
    private final OrderItemRepository orderItems;

    public CheckoutService(CurrentUserService currentUserService,
                           CartRepository carts,
                           CartItemRepository cartItems,
                           OrderRepository orders,
                           OrderItemRepository orderItems) {
        this.currentUserService = currentUserService;
        this.carts = carts;
        this.cartItems = cartItems;
        this.orders = orders;
        this.orderItems = orderItems;
    }

    @Transactional
    public OrderDtos.OrderResponse createOrderFromCart() {
        User user = currentUserService.requireUser();
        Cart cart = carts.findByUserId(user.getId()).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        List<CartItem> items = cartItems.findByCartId(cart.getId());
        if (items.isEmpty()) throw new IllegalArgumentException("Cart is empty");

        Order order = new Order(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order = orders.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : items) {
            Product p = ci.getProduct();

            // We only validate inventory now; we actually reduce inventory after payment success (Step 5.2)
            if (!p.isActive()) throw new IllegalArgumentException("Product inactive: " + p.getName());
            if (p.getInventory() < ci.getQuantity()) throw new IllegalArgumentException("Not enough inventory for: " + p.getName());

            BigDecimal line = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(line);

            OrderItem oi = new OrderItem(
                    order,
                    p,
                    p.getSku(),
                    p.getName(),
                    ci.getUnitPrice(),
                    ci.getQuantity(),
                    line
            );
            orderItems.save(oi);
        }

        order.setTotal(total);
        orders.save(order);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> myOrders() {
        User user = currentUserService.requireUser();
        return orders.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse myOrder(Long id) {
        User user = currentUserService.requireUser();
        Order order = orders.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return toResponse(order);
    }

    private OrderDtos.OrderResponse toResponse(Order order) {
        List<OrderItem> items = orderItems.findByOrderId(order.getId());

        List<OrderDtos.OrderItemResponse> itemResponses = items.stream().map(oi ->
                new OrderDtos.OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getSku(),
                        oi.getName(),
                        oi.getUnitPrice(),
                        oi.getQuantity(),
                        oi.getLineTotal()
                )
        ).toList();

        return new OrderDtos.OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getCurrency(),
                order.getTotal(),
                itemResponses
        );
    }
}
