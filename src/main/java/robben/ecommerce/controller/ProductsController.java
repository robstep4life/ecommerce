package robben.ecommerce.controller;

import robben.ecommerce.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productService.listActive(page, size);
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        return productService.getActive(id);
    }

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam("q") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productService.searchActiveByName(q, page, size);
    }
}
