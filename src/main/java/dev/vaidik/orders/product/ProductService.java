package dev.vaidik.orders.product;

import dev.vaidik.orders.common.ConflictException;
import dev.vaidik.orders.common.NotFoundException;
import dev.vaidik.orders.product.ProductDtos.ProductRequest;
import dev.vaidik.orders.product.ProductDtos.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Page<ProductResponse> search(String query, Pageable pageable) {
        Page<Product> page = (query == null || query.isBlank())
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(query.trim(), pageable);
        return page.map(ProductResponse::from);
    }

    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw new ConflictException("A product with SKU " + request.sku() + " already exists");
        }
        Product product = new Product(request.sku(), request.name(), request.description(),
                request.price(), request.stock());
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getSku().equals(request.sku())) {
            throw new ConflictException("SKU cannot be changed");
        }
        product.update(request.name(), request.description(), request.price(), request.stock());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    Product find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }
}
