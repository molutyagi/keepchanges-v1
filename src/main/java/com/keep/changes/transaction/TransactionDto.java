package com.keep.changes.transaction;

import lombok.Data;

@Data
public class TransactionDto {
	private Long id;
	private String razorpayOrderId; // Unique ID from Razorpay
	private String razorpayPaymentId; // Unique ID for the payment within Razorpay
	private Double totalAmount; // Transaction amount
	private Double donationAmount; // Donation amount
	private Double tipAmount; // Tip amount
	private String currency; // Transaction currency (e.g., INR, USD)
	private String status; // Payment status (e.g., "captured", "authorized", "failed")
	private String createdAt; // Timestamp when transaction was created in Razorpay
	// Additional fields (optional)
	private String method; // Payment method used (e.g., "card", "netbanking")
	private String notes; // Any notes from Razorpay
	private String error_code; // Error code if transaction failed
	private String error_description; // Error description if transaction failed

	// Donor Details
	private String name;
	private String email;
	private String phone;

	// Associated Fundraiser
	private Long fundraiserId;

}
