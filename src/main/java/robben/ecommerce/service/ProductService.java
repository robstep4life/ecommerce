package robben.ecommerce.service;

import robben.ecommerce.dto.ProductDtos;
import robben.ecommerce.entity.Product;
import robben.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    // ===== PUBLIC =====
    public Map<String, Object> listActive(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var result = products.findByActiveTrue(pageable);

        List<ProductDtos.ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();

        return Map.of(
                "items", items,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalItems", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }

    public Map<String, Object> searchActiveByName(String q, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var result = products.findByActiveTrueAndNameContainingIgnoreCase(q, pageable);

        List<ProductDtos.ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();

        return Map.of(
                "items", items,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalItems", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }

    public ProductDtos.ProductResponse getActive(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!p.isActive()) throw new IllegalArgumentException("Product not found");
        return toResponse(p);
    }

    // ===== ADMIN =====
    public Map<String, Object> adminListAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var result = products.findAll(pageable);

        List<ProductDtos.ProductResponse> items = result.getContent().stream().map(this::toResponse).toList();

        return Map.of(
                "items", items,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalItems", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }

    public ProductDtos.ProductResponse adminGet(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return toResponse(p);
    }

    public ProductDtos.ProductResponse create(ProductDtos.ProductCreateRequest req) {
        String sku = req.sku().trim();
        if (products.existsBySku(sku)) throw new IllegalArgumentException("SKU already exists");

        Product p = new Product();
        p.setSku(sku);
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setInventory(req.inventory());
        if (req.active() != null) p.setActive(req.active());

        return toResponse(products.save(p));
    }

    public ProductDtos.ProductResponse update(Long id, ProductDtos.ProductUpdateRequest req) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (req.name() != null) p.setName(req.name().trim());
        if (req.description() != null) p.setDescription(req.description());
        if (req.price() != null) p.setPrice(req.price());
        if (req.inventory() != null) p.setInventory(req.inventory());
        if (req.active() != null) p.setActive(req.active());

        return toResponse(products.save(p));
    }

    // Soft delete (deactivate)
    public void deactivate(Long id) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        p.setActive(false);
        products.save(p);
    }

    private ProductDtos.ProductResponse toResponse(Product p) {
        return new ProductDtos.ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getInventory(),
                p.isActive()
        );
    }
}
