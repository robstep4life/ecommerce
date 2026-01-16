package robben.ecommerce.controller;

import robben.ecommerce.dto.OrderDtos;
import robben.ecommerce.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final CheckoutService checkoutService;

    public OrdersController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/my")
    public List<OrderDtos.OrderResponse> myOrders() {
        return checkoutService.myOrders();
    }

    @GetMapping("/{id}")
    public OrderDtos.OrderResponse myOrder(@PathVariable Long id) {
        return checkoutService.myOrder(id);
    }
}
