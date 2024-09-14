package com.keep.changes.fundraiser;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.keep.changes.fundraiser.document.FundraiserDocumentDto;
import com.keep.changes.fundraiser.photo.PhotoDto;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.Email;
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
public class FundraiserDetailsResponse {
	private Long id;

	@NotEmpty
	private String fundraiserTitle;

	@NotEmpty
	private String fundraiserDescription;

	@NotEmpty
	private String beneficiary;

	@NonNull
	private Double raiseGoal;

	@JsonProperty(access = Access.READ_ONLY)
	private Double raised;

	@NotEmpty
	@Email(message = "Given email is not valid.")
	@Pattern(regexp = "^([a-zA-Z0-9._%-]{4,}+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Given email is not valid.")
	private String email;

	@NotEmpty
	@Pattern(regexp = "(0|91)?[6-9][0-9]{9}", message = "Invalid Number Format.")
	private String phone;

	private String displayPhoto;

	private AdminApproval approval;

	private String adminRemarks;

	private FundraiserStatus status;

	private Boolean isActive;

	private Date endDate;

	private Long categoryId;

	private String categoryName;

	private Long postedById;

	private String postedByName;

	private Set<PhotoDto> photos;

	private Set<FundraiserDocumentDto> documents;

	private String accountNumber;

	private String bankName;

	@JsonProperty("donations")
	private Set<FundraiserDonationDto> transactions;

}
