package com.example.ingenieur.cerberus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.example.ingenieur.cerberus.Controller.ProductController;
import com.example.ingenieur.cerberus.Entity.Product;
import com.example.ingenieur.cerberus.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductControllerTests {
    private ProductRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(repository)).build();
    }

    @Test
    void createsProduct() throws Exception {
        Product saved = new Product("Keyboard", "Mechanical keyboard", 79.99);
        saved.setId(1L);
        when(repository.save(any(Product.class))).thenReturn(saved);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Keyboard","description":"Mechanical keyboard","price":79.99}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.price").value(79.99));
    }

    @Test
    void returnsAllProducts() throws Exception {
        Product product = new Product("Mouse", "Wireless mouse", 25.0);
        product.setId(2L);
        when(repository.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Mouse"));
    }

    @Test
    void returnsProductById() throws Exception {
        Product product = new Product("Monitor", "27 inch", 199.0);
        product.setId(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monitor"));
    }

    @Test
    void returns404WhenProductDoesNotExistOnRead() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesExistingProduct() throws Exception {
        Product product = new Product("Old name", "Old description", 10.0);
        product.setId(4L);
        when(repository.findById(4L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        mockMvc.perform(put("/api/products/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name","description":"New description","price":15.5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.product.name").value("New name"))
                .andExpect(jsonPath("$.product.price").value(15.5));
    }

    @Test
    void returns404WhenUpdatingMissingProduct() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Missing\",\"description\":\"N/A\",\"price\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void deletesExistingProduct() throws Exception {
        when(repository.existsById(5L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"))
                .andExpect(jsonPath("$.id").value(5));
        verify(repository).deleteById(5L);
    }

    @Test
    void returns404WhenDeletingMissingProduct() throws Exception {
        when(repository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.id").value(99));
        verify(repository, never()).deleteById(any());
    }
}
