package com.keep.changes.transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {

	String createOrder(OrderRequest transactionRequest);

	TransactionDto saveTransaction(Long fId, TransactionDto dto);

	List<Transaction> getAllTransactions();

	CompletableFuture<Boolean> verifyTransaction(String razorpay_payment_id, String razorpay_order_id, String razorpay_signature);
}
