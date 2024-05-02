package com.keep.changes.donation;

import java.sql.Date;

import com.keep.changes.fundraiser.FundraiserDto;
import com.keep.changes.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FundraiserDonationDto {

	private Long id;

	private Double donationAmount;
	
	private String transactionId;

	private Date donationDate;

	private UserDto donor;

	private FundraiserDto fundraiser;

}
