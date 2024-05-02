package com.keep.changes.address;

import java.util.HashSet;
import java.util.Set;

import com.keep.changes.fundraiser.FundraiserDto;
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
public class AddressDto {

	private Long id;

	@NotEmpty
	private String area;

	@NotEmpty
	private String city;

	@NotEmpty
	private String state;

	@NotEmpty
	private String country;

	@NotEmpty
	private String pincode;

//	private Set<UserDto> associatedUser = new HashSet<>();

//	private Set<FundraiserDto> associatedFundraiser = new HashSet<>();

}