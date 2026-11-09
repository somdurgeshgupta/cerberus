package com.example.ingenieur.cerberus.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.ingenieur.cerberus.Entity.Product;
import com.example.ingenieur.cerberus.Repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    public final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }
    // CREATE
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return repository.save(product);
    }

    // READ (All)
    @GetMapping
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    // READ (Single)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return repository.findById(id)
                .map(product -> ResponseEntity.ok().body(product))
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @RequestBody Product details) {
        return repository.findById(id).map(product -> {
            product.setName(details.getName());
            product.setDescription(details.getDescription());
            product.setPrice(details.getPrice());
            Product updated = repository.save(product);
            return ResponseEntity.ok(Map.<String, Object>of(
                    "message", "Product updated successfully",
                    "product", updated));
        }).orElseGet(() -> ResponseEntity.status(404)
                .body(Map.<String, Object>of("message", "Product not found", "id", id)));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Product not found", "id", id));
        }

        repository.deleteById(id);
        return ResponseEntity.ok(
                Map.of("message", "Product deleted successfully", "id", id));
    }
}
