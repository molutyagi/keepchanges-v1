package com.keep.changes.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOtpDto {

	private String email;
	private String phone;
	private String name;

}
