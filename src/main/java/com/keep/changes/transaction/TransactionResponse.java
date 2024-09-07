package com.keep.changes.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

	private String razorpay_payment_id;
	private String razorpay_order_id;
	private String razorpay_signature;

}
