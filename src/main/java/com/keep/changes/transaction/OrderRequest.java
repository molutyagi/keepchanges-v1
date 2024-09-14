package com.keep.changes.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderRequest {

	private String currency;
	private Double totalAmount;

	private String name;
	private String email;
	private String phone;

	private Long fundraiserId;
	private Double donationAmount;
	private Double tipAmount;

}
