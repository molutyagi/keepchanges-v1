package com.keep.changes.fundraiser;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keep.changes.account.AccountDto;
import com.keep.changes.category.CategoryDto;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class FundraiserDto {
	private Long id;

	@NotEmpty
	private String fundraiserTitle;

	@NotEmpty
	private String fundraiserDescription;

	@NotEmpty
	private String beneficiary;

	@NonNull
	private Double raiseGoal;

	@NotEmpty
	@Email(message = "Given email is not valid.")
	@Pattern(regexp = "^([a-zA-Z0-9._%-]{4,}+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Given email is not valid.")
	private String email;

	@NotEmpty
	@Pattern(regexp = "(0|91)?[6-9][0-9]{9}", message = "Invalid Number Format.")
	private String phone;

	@DateTimeFormat(pattern = "dd-MM-yyyy")
	@Future(message = "End date can only be in future")
	private Date endDate;

//	@NotEmpty
	private String displayPhoto;

	@NonNull
	private CategoryDto category;

	@JsonIgnore
	private AccountDto account;

}
