package com.keep.changes.transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);

}
