package robben.ecommerce.controller;

import robben.ecommerce.dto.OrderDtos;
import robben.ecommerce.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public OrderDtos.OrderResponse checkout() {
        return checkoutService.createOrderFromCart();
    }
}
