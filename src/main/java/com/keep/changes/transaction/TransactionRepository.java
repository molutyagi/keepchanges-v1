package com.keep.changes.transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);

}
