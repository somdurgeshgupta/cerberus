package com.example.ingenieur.cerberus.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ingenieur.cerberus.Entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByProductId(Long productId);
}

