package robben.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class CartDtos {

    public record AddItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record UpdateItemRequest(
            @NotNull @Min(0) Integer quantity
    ) {}

    public record CartItemResponse(
            Long itemId,
            Long productId,
            String sku,
            String name,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {}

    public record CartResponse(
            Long cartId,
            List<CartItemResponse> items,
            BigDecimal total
    ) {}
}
