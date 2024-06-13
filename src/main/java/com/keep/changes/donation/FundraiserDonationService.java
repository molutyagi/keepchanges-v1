package com.keep.changes.donation;

import java.util.List;

public interface FundraiserDonationService {

//	add a donation
	FundraiserDonationDto addFundraiserDonation(Long fId, FundraiserDonationDto donationDto);

//	update a donation
	FundraiserDonationDto putUpdateDonation(Long fDId, FundraiserDonationDto donationDto);

	FundraiserDonationDto patchUpdateDonation(Long fDId, FundraiserDonationDto partcialDonationDto);

//	delete a donation
	void deleteDonation(Long fDId);

//	get
	List<FundraiserDonationDto> getAllDonations();

	FundraiserDonationDto getDonationById(Long fDId);

	List<FundraiserDonationDto> getAllDonationsByFundraiser(Long fId);

	List<FundraiserDonationDto> getAllDonationsByDonor(Long dId);

//	admin dashboard
	Long totalDonations();
	Long totalDonors();
	Long totalDonatedFundraisers();


}
