package com.keep.changes.admin;

import lombok.Data;

@Data
public class FundraiserDashboardDto {

	private Long totalFundraisers;

	private Long totalActiveFundraisers;
	
	private Long createdThisMonth;

}
