package com.keep.changes.transaction;

import lombok.Data;

@Data
public class TransactionResponse {

	private String razorpay_payment_id;
	private String razorpay_order_id;
	private String razorpay_signature;

}
