package com.keep.changes.fundraiser;

import java.util.List;

import jakarta.validation.Valid;

public interface FundraiserService {

//	Add Fundraiser
	FundraiserDto createFundraiser(FundraiserDto fundraiserDto);

//	Put Update Fundraiser
	FundraiserDto putUpdateFundraiser(Long fId, FundraiserDto fundraiserDto);

//	Patch Update Fundraiser
	FundraiserDto patchFundraiser(Long fId, FundraiserDto fundraiserDto);

//	Delete Fundraiser
	void deleteFundraiser(Long fId);

//	delete display
	boolean deleteDisplay(@Valid Long fId);

//	Get Fundraiser
	FundraiserDto getFundraiserById(Long fId);

	List<FundraiserDto> getAllFundraisers();

	List<FundraiserDto> getFundraiserByEmail(String email);

	List<FundraiserDto> getFundraiserByPhone(String phone);

	List<FundraiserDto> getFundraisersByTitle(String title);

	List<FundraiserDto> getFundraisersByCategory(Long categoryId);

	List<FundraiserDto> getFundraisersByPoster(String username);

	void fundraiserAdminService(@Valid Long fId, String adminRemarks, AdminApproval adminStatus);

}
