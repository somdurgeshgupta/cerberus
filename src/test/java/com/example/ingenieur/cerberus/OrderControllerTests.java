package com.example.ingenieur.cerberus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ingenieur.cerberus.Controller.OrderController;
import com.example.ingenieur.cerberus.Entity.OrderStatus;
import com.example.ingenieur.cerberus.Service.OrderService;
import com.example.ingenieur.cerberus.dto.OrderResponse;
import com.example.ingenieur.cerberus.dto.PlaceOrderRequest;
import com.example.ingenieur.cerberus.exception.GlobalExceptionHandler;
import com.example.ingenieur.cerberus.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderControllerTests {
    private OrderService orderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void placesOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, 10L, "Keyboard", 79.99, 2, 159.98, OrderStatus.PLACED, LocalDateTime.now());
        when(orderService.placeOrder(any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":10,"quantity":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.productName").value("Keyboard"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(159.98))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    void returns400WhenPlaceOrderRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":null,"quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.productId").value("Product ID is required"))
                .andExpect(jsonPath("$.errors.quantity").value("Quantity must be at least 1"));

        verify(orderService, never()).placeOrder(any());
    }

    @Test
    void returns404WhenPlacingOrderForMissingProduct() throws Exception {
        when(orderService.placeOrder(any(PlaceOrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found", "productId", 99L));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":99,"quantity":1}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.productId").value(99));
    }

    @Test
    void returnsAllOrders() throws Exception {
        OrderResponse order = new OrderResponse(
                2L, 20L, "Mouse", 25.0, 2, 50.0, OrderStatus.PLACED, LocalDateTime.now());
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].productName").value("Mouse"))
                .andExpect(jsonPath("$[0].totalPrice").value(50.0));
    }

    @Test
    void returnsOrderById() throws Exception {
        OrderResponse order = new OrderResponse(
                3L, 30L, "Monitor", 199.0, 1, 199.0, OrderStatus.PLACED, LocalDateTime.now());
        when(orderService.getOrderById(3L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.productName").value("Monitor"));
    }

    @Test
    void returns404WhenOrderDoesNotExistOnRead() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new ResourceNotFoundException("Order not found", "id", 99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void deletesExistingOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(5L);

        mockMvc.perform(delete("/api/orders/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order deleted successfully"))
                .andExpect(jsonPath("$.id").value(5));

        verify(orderService).deleteOrder(5L);
    }

    @Test
    void returns404WhenDeletingMissingOrder() throws Exception {
        doThrow(new ResourceNotFoundException("Order not found", "id", 99L))
                .when(orderService).deleteOrder(99L);

        mockMvc.perform(delete("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.id").value(99));
    }
}

