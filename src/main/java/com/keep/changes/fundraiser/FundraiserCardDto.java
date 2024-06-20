package com.keep.changes.fundraiser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FundraiserCardDto {
	private Long id;

	@NotEmpty
	private String fundraiserTitle;

//	@NotEmpty
//	private String fundraiserDescription;

	@NotEmpty
	private String beneficiary;

	@NonNull
	private Double raiseGoal;

	@JsonProperty(access = Access.READ_ONLY)
	private Double raised;

//	@NotEmpty
	private String displayPhoto;

	@JsonProperty(access = Access.READ_ONLY)
	private String postedByName;

	@JsonProperty(access = Access.READ_ONLY)
	private FundraiserStatus status;

	@Override
	public String toString() {
		return "FundraiserDto [id=" + id + ", fundraiserTitle=" + fundraiserTitle + ", beneficiary=" + beneficiary
				+ ", raiseGoal=" + raiseGoal + ", raised=" + raised + ", displayPhoto=" + displayPhoto;
	}

}
