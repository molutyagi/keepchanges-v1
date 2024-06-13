package com.keep.changes.fundraiser;

import java.sql.Date;

import com.keep.changes.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DonationDto {

	private Long id;

	private Double donationAmount;

	private String transactionId;

	private Date donationDate;

	private UserDto donor;
}