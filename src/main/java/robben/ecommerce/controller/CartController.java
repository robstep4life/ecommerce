package robben.ecommerce.controller;

import jakarta.validation.Valid;
import robben.ecommerce.dto.CartDtos;
import robben.ecommerce.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDtos.CartResponse getMyCart() {
        return cartService.getMyCart();
    }

    @PostMapping("/items")
    public CartDtos.CartResponse addItem(@Valid @RequestBody CartDtos.AddItemRequest req) {
        return cartService.addItem(req);
    }

    @PutMapping("/items/{itemId}")
    public CartDtos.CartResponse updateItem(@PathVariable Long itemId, @Valid @RequestBody CartDtos.UpdateItemRequest req) {
        return cartService.updateItem(itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    public CartDtos.CartResponse removeItem(@PathVariable Long itemId) {
        return cartService.removeItem(itemId);
    }

    @DeleteMapping("/clear")
    public CartDtos.CartResponse clear() {
        return cartService.clear();
    }
}
