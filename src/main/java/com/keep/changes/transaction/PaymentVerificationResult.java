package com.keep.changes.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentVerificationResult {
	private boolean isVerified;
	private String paymentId;
	private Double totalAmount;
	private Double tipAmount;
	private Double donationAmount;
	private String status;
}
