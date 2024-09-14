package com.keep.changes.fundraiser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class FundraiserDonationDto {

	private Long id;

	private Double donationAmount;

	private String name;
}