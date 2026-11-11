package com.example.ingenieur.cerberus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.ingenieur.cerberus.Entity.Order;
import com.example.ingenieur.cerberus.Entity.OrderStatus;
import com.example.ingenieur.cerberus.Entity.Product;
import com.example.ingenieur.cerberus.Repository.OrderRepository;
import com.example.ingenieur.cerberus.Repository.ProductRepository;
import com.example.ingenieur.cerberus.Service.OrderService;
import com.example.ingenieur.cerberus.dto.OrderResponse;
import com.example.ingenieur.cerberus.dto.PlaceOrderRequest;
import com.example.ingenieur.cerberus.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTests {
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        orderService = new OrderService(orderRepository, productRepository);
    }

    @Test
    void placesOrderAndCalculatesTotalPrice() {
        Product product = new Product("Keyboard", "Mechanical keyboard", 79.99);
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        OrderResponse response = orderService.placeOrder(new PlaceOrderRequest(1L, 3));

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getProductId());
        assertEquals("Keyboard", response.getProductName());
        assertEquals(79.99, response.getUnitPrice());
        assertEquals(3, response.getQuantity());
        assertEquals(239.97, response.getTotalPrice());
        assertEquals(OrderStatus.PLACED, response.getStatus());
        assertNotNull(response.getCreatedAt());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void throwsResourceNotFoundWhenPlacingOrderForMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.placeOrder(new PlaceOrderRequest(99L, 2)));

        assertEquals("Product not found", ex.getMessage());
        assertEquals("productId", ex.getField());
        assertEquals(99L, ex.getResourceId());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void returnsAllOrders() {
        Product product = new Product("Mouse", "Wireless mouse", 25.0);
        product.setId(2L);
        Order order = new Order(product, 2, 50.0, OrderStatus.PLACED, LocalDateTime.now());
        order.setId(11L);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> orders = orderService.getAllOrders();

        assertEquals(1, orders.size());
        assertEquals(11L, orders.get(0).getId());
        assertEquals("Mouse", orders.get(0).getProductName());
        assertEquals(50.0, orders.get(0).getTotalPrice());
    }

    @Test
    void returnsOrderById() {
        Product product = new Product("Monitor", "27 inch", 199.0);
        product.setId(3L);
        Order order = new Order(product, 1, 199.0, OrderStatus.PLACED, LocalDateTime.now());
        order.setId(12L);
        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(12L);

        assertEquals(12L, response.getId());
        assertEquals("Monitor", response.getProductName());
        assertEquals(199.0, response.getTotalPrice());
    }

    @Test
    void throwsResourceNotFoundWhenOrderDoesNotExistOnRead() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(99L));

        assertEquals("Order not found", ex.getMessage());
        assertEquals("id", ex.getField());
        assertEquals(99L, ex.getResourceId());
    }

    @Test
    void deletesExistingOrder() {
        when(orderRepository.existsById(5L)).thenReturn(true);

        orderService.deleteOrder(5L);

        verify(orderRepository).deleteById(5L);
    }

    @Test
    void throwsResourceNotFoundWhenDeletingMissingOrder() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.deleteOrder(99L));

        assertEquals("Order not found", ex.getMessage());
        assertEquals("id", ex.getField());
        assertEquals(99L, ex.getResourceId());
        verify(orderRepository, never()).deleteById(any());
    }
}

