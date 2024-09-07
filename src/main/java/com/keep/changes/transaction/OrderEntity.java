package com.keep.changes.transaction;

import com.keep.changes.fundraiser.Fundraiser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String currency;
	private Double totalAmount;

	private Long fundraiserId;
	private Double donationAmount;
	private Double tipAmount;

	private String razorpayOrderId;

	// Donor Details
	private String name;
	private String email;
	private String phone;

}