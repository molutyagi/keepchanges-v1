package com.keep.changes.fundraiser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FundraiserStatus {
	ACTIVE("Active"), COMPLETED("Completed"), CANCELLED("Cancelled"), INACTIVE("Inactive");

	private final String status;
}
