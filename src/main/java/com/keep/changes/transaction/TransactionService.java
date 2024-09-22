package com.keep.changes.transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;

public interface TransactionService {

	String createOrder(OrderRequest transactionRequest);

	TransactionDto saveTransaction(Long fId, TransactionDto dto);

	List<Transaction> getAllTransactions();

	CompletableFuture<PaymentVerificationResult> verifyTransaction(String razorpay_payment_id, String razorpay_order_id,
			String razorpay_signature);

//	ResponseEntity<Void> redirectToPaymentResult(PaymentVerificationResult result);
}
