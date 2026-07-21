package com.lms.payment.repository;

import com.lms.payment.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
