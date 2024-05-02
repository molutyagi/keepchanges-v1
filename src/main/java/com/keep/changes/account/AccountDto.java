package com.keep.changes.account;

import com.keep.changes.user.UserDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountDto {

	private Long id;

	@NotEmpty
	private String accountNumber;

	@NotEmpty
	private String ifsc;

	@NotEmpty
	private String bankName;

	@NotEmpty
	private String branch;

	@NotEmpty
	private String holderName;

//	@NotEmpty
	private UserDto holdingEntity;

//	private Set<FundraiserDto> associatedFundraiser = new HashSet<>();

}