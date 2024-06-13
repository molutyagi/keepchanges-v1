package com.keep.changes.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.donation.FundraiserDonationService;
import com.keep.changes.fundraiser.FundraiserService;

@RestController
@RequestMapping("admin/dashboard")
public class AdminDashboardController {

	@Autowired
	private FundraiserService fundraiserService;

	@Autowired
	private FundraiserDonationService donationService;

	@GetMapping(value = { "fundraisers", "fundraisers/" })
	public ResponseEntity<FundraiserDashboardDto> fundraisersDashboard() {
		FundraiserDashboardDto fDD = new FundraiserDashboardDto();

		long totalFundraisers = this.fundraiserService.totalFundraisers();
		long totalActiveFundraisers = this.fundraiserService.totalActiveFundraisers();
		long createdThisMonth = 0;
		
		fDD.setTotalFundraisers(totalFundraisers);
		fDD.setTotalActiveFundraisers(totalActiveFundraisers);
		fDD.setCreatedThisMonth(createdThisMonth);

		return ResponseEntity.ok(fDD);

	}

	@GetMapping(value = { "donations", "donations/" })
	public ResponseEntity<DonationDashboardDto> donationsDashboard() {

		DonationDashboardDto dDD = new DonationDashboardDto();
		long sumOfRaised = Math.round(this.fundraiserService.sumOfRaised());
		long sumOfRaiseGoal = Math.round(this.fundraiserService.sumOfRaiseGoal());
		long totalDonations = this.donationService.totalDonations();
		long totalDonors = this.donationService.totalDonors();
		Long totalDonatedFundraisers = this.donationService.totalDonatedFundraisers();
		long donationsThisWeek = 0;

		dDD.setSumOfRaised(sumOfRaised);
		dDD.setSumOfRaiseGoal(sumOfRaiseGoal);
		dDD.setTotalDonations(totalDonations);
		dDD.setTotalDonors(totalDonors);
		dDD.setDonatedFundraisers(totalDonatedFundraisers);
		dDD.setDonationsThisWeek(donationsThisWeek);

		return ResponseEntity.ok(dDD);

	}
}
