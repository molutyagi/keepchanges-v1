package com.keep.changes.fundraiser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FundraiserStatus {
	ACTIVE("Active"), OPEN("Open"), COMPLETED("Completed"), CANCELLED("Cancelled"), INACTIVE("Inactive"),
	CLOSED("Closed");

	private final String status;
}
