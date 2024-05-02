package com.keep.changes.pan;

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
public class PanDto {

	private Long id;

	@NotEmpty
	private String panNumber;

	@NotEmpty
	private String nameOnPan;

//	@NotEmpty
//	private String panImage;

	private UserDto panHolder;

	@NotEmpty
	private String address;

	@NotEmpty
	private String city;

	@NotEmpty
	private String state;

	@NotEmpty
	private String country;

	@NotEmpty
	private String pincode;

	private Set<FundraiserDto> fundraisers;

}
