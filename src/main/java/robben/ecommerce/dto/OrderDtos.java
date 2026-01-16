package robben.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderDtos {

    public record OrderItemResponse(
            Long productId,
            String sku,
            String name,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            Long orderId,
            String status,
            String currency,
            BigDecimal total,
            List<OrderItemResponse> items
    ) {}
}
