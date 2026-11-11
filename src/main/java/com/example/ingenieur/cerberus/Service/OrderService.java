package com.example.ingenieur.cerberus.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ingenieur.cerberus.Entity.Order;
import com.example.ingenieur.cerberus.Entity.OrderStatus;
import com.example.ingenieur.cerberus.Entity.Product;
import com.example.ingenieur.cerberus.Repository.OrderRepository;
import com.example.ingenieur.cerberus.Repository.ProductRepository;
import com.example.ingenieur.cerberus.dto.OrderResponse;
import com.example.ingenieur.cerberus.dto.PlaceOrderRequest;
import com.example.ingenieur.cerberus.exception.ResourceNotFoundException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found", "productId", request.getProductId()));

        double totalPrice = BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        Order order = new Order(
                product,
                request.getQuantity(),
                totalPrice,
                OrderStatus.PLACED,
                LocalDateTime.now()
        );

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found", "id", id));
        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found", "id", id);
        }
        orderRepository.deleteById(id);
    }
}

