package robben.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDtos {

    public record ProductCreateRequest(
            @NotBlank @Size(max = 64) String sku,
            @NotBlank @Size(max = 200) String name,
            String description,
            @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
            @NotNull @Min(0) Integer inventory,
            Boolean active
    ) {}

    public record ProductUpdateRequest(
            @Size(max = 200) String name,
            String description,
            @DecimalMin(value = "0.00", inclusive = false) BigDecimal price,
            @Min(0) Integer inventory,
            Boolean active
    ) {}

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            Integer inventory,
            boolean active
    ) {}
}
