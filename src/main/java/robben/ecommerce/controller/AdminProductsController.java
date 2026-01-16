package robben.ecommerce.controller;

import jakarta.validation.Valid;
import robben.ecommerce.dto.ProductDtos;
import robben.ecommerce.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductsController {

    private final ProductService productService;

    public AdminProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productService.adminListAll(page, size);
    }

    @GetMapping("/{id}")
    public ProductDtos.ProductResponse get(@PathVariable Long id) {
        return productService.adminGet(id);
    }

    @PostMapping
    public ProductDtos.ProductResponse create(@Valid @RequestBody ProductDtos.ProductCreateRequest req) {
        return productService.create(req);
    }

    @PutMapping("/{id}")
    public ProductDtos.ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductDtos.ProductUpdateRequest req) {
        return productService.update(id, req);
    }

    // Soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
