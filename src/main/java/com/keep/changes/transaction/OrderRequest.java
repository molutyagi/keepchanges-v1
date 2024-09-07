package com.keep.changes.transaction;

import lombok.Data;

@Data
public class TransactionRequest {

	private Long fundraiserId;
	private Double totalAmount;
	private String currency;

	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String razorpaySignature;

}
