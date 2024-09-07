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

import com.keep.changes.payload.response.ApiResponse;
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
	public CompletableFuture<ResponseEntity<ApiResponse>> verifyTransaction(
			@RequestParam("razorpay_payment_id") String razorpay_payment_id,
			@RequestParam("razorpay_order_id") String razorpay_order_id,
			@RequestParam("razorpay_signature") String razorpay_signature) {

		return this.service.verifyTransaction(razorpay_payment_id, razorpay_order_id, razorpay_signature)
				.thenApply(isVerified -> {
					if (isVerified) {
						HttpHeaders headers = new HttpHeaders();
						headers.setLocation(URI.create("http://localhost:5173/donation/successful"));
						return new ResponseEntity<>(new ApiResponse("Payment verified.", isVerified), headers,
								HttpStatus.FOUND);
					} else {
						HttpHeaders headers = new HttpHeaders();
						headers.setLocation(URI.create("http://localhost:5173/donation/failed"));
						return new ResponseEntity<>(new ApiResponse("Payment failed.", isVerified), headers,
								HttpStatus.FOUND);
					}
				});
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
