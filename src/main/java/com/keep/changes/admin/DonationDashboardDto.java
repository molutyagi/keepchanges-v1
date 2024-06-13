package com.keep.changes.admin;

import lombok.Data;

@Data
public class DonationDashboardDto {

	private Long sumOfRaised;
	private Long sumOfRaiseGoal;
	private Long totalDonations;
	private Long totalDonors;
	private Long donatedFundraisers;
	private Long donationsThisWeek;
	

}
