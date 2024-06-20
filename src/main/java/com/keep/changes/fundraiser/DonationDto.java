package com.keep.changes.fundraiser;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DonationDto {

	private Long id;

	private Double donationAmount;

	private Date donationDate;
	
	private String donorName;
	
	private String donorEmail;
}