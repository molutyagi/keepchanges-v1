package com.keep.changes.transaction;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("transactions")
public class TransactionController {

	@Autowired
	private TransactionService service;

	@PostMapping("create-order")
	public ResponseEntity<String> createTransactionOrder(@RequestBody OrderRequest request) {

		System.out.println("order request: " + request.toString());
		return ResponseEntity.ok(this.service.createOrder(request));
	}

	@PostMapping("verify-transaction")
	public CompletableFuture<ResponseEntity<PaymentVerificationResult>> verifyTransaction(
			@RequestParam("razorpay_payment_id") String razorpay_payment_id,
			@RequestParam("razorpay_order_id") String razorpay_order_id,
			@RequestParam("razorpay_signature") String razorpay_signature) {

		System.out.println("in verify transaction controller");

		return this.service.verifyTransaction(razorpay_payment_id, razorpay_order_id, razorpay_signature)
				.thenApply(this::createPaymentSuccessResponse);

	}

	private ResponseEntity<PaymentVerificationResult> createPaymentSuccessResponse(PaymentVerificationResult result) {

		System.out.println("RESULT: " + result.getStatus());
		System.out.println(result);

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString("http://localhost:5173/fundraiser/payment-success");
		URI uri = builder.build().toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uri);
		return new ResponseEntity<>(result, headers, HttpStatus.FOUND);
	}

	@PostMapping("save-transaction/{fId}")
	public ResponseEntity<TransactionDto> saveTransaction(@Valid @PathVariable("fId") Long fId,
			@RequestBody TransactionDto dto) {

		System.out.println(dto);

		return ResponseEntity.ok(this.service.saveTransaction(fId, dto));
	}

	@GetMapping("get-all")
	public ResponseEntity<List<Transaction>> getAll() {
		return ResponseEntity.ok(this.service.getAllTransactions());
	}
}
