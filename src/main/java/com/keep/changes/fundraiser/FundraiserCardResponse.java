package com.keep.changes.fundraiser;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FundraiserCardResponse {

	private List<FundraiserCardDto> fundraisers;
	private Integer pageNo;
	private Integer pageSize;
	private long totalElements;
	private Integer totalPages;
	private boolean isLastPage;

}
