package com.keep.changes.fundraiser;

import java.util.List;

import org.springframework.data.domain.Page;
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
	
	FundraiserDetailsResponse getFundraiserById1(Long fId);


	FundraiserCardResponse getAllFundraisers(Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getAllActiveFundraisers(Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getLatestFundraiser();

	FundraiserCardResponse getFundraiserByEmail(String email, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraiserByPhone(String phone, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraisersByTitle(String title, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraisersByCategory(Long categoryId, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraisersByPoster(String username, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraisersByPosterId(Long pId, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getActiveFundraisersByPosterId(Long pId, Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getActive100Fundraisers(Integer pageNumber, Integer pageSize);

	void fundraiserAdminService(@Valid Long fId, String adminRemarks, AdminApproval adminStatus);

//	admin dashboard
	Double sumOfRaised();

	Double sumOfRaiseGoal();

	Long totalFundraisers();

	Long totalActiveFundraisers();

	FundraiserCardResponse findByIsReviewedFalse(Integer pageNumber, Integer pageSize);

	FundraiserCardResponse getFundraisersByCategories(Long[] categoryId, Integer pageNumber, Integer pageSize);
}
