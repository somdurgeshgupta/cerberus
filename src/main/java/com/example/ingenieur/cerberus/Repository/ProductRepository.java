package com.example.ingenieur.cerberus.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ingenieur.cerberus.Entity.Product;


public interface ProductRepository extends JpaRepository<Product, Long> {
}
