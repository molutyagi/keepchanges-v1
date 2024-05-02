package com.keep.changes.donation;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.exception.ApiException;
import com.keep.changes.payload.response.ApiResponse;
import com.keep.changes.user.UserDto;

//import com.keep.changes.fundraiser.FundraiserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/")
public class FundraiserDonationController {

	@Autowired
	private FundraiserDonationService donationService;

//	@Autowired
//	private FundraiserService fundraiserService;

	@PostMapping(value = { "fundraisers/fundraiser_{fId}/add", "fundraisers/fundraiser_{fId}/add/" })
	public ResponseEntity<FundraiserDonationDto> addDonation(@Valid @PathVariable Long fId,
			@RequestBody FundraiserDonationDto donationDto) {

		return ResponseEntity.ok(this.donationService.addFundraiserDonation(fId, donationDto));
	}

	@PutMapping(value = { "fundraisers/fundraiser_{fId}/donation_{dId}",
			"fundraisers/fundraiser_{fId}/donation_{dId}/" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> putUpdateDonation(@Valid @PathVariable Long fId, @PathVariable Long dId,
			@RequestBody FundraiserDonationDto donationDto) {

		return ResponseEntity.ok(this.donationService.putUpdateDonation(dId, donationDto));
	}

	@PatchMapping(value = { "fundraisers/fundraiser_{fId}/donation_{dId}",
			"fundraisers/fundraiser_{fId}/donation_{dId}/" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> patchUpdateDonation(@Valid @PathVariable Long fId, @PathVariable Long dId,
			@RequestBody FundraiserDonationDto donationDto) {

		return ResponseEntity.ok(this.donationService.patchUpdateDonation(dId, donationDto));
	}

	@DeleteMapping(value = { "fundraisers/fundraiser_{fId}/donation_{dId}",
			"fundraisers/fundraiser_{fId}/donation_{dId}/" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteDonation(@Valid @PathVariable Long fId, @PathVariable Long dId) {
		this.donationService.deleteDonation(dId);
		return ResponseEntity.ok(new ApiResponse("Fundraiser Donation data deleted successfully.", true));
	}

//	get 
//	all
	@GetMapping(value = { "donations", "donations/", "donations/getall", "donations/getall/" })
	public ResponseEntity<List<FundraiserDonationDto>> getAllDonations() {
		return ResponseEntity.ok(this.donationService.getAllDonations());
	}

//	by id
	@GetMapping(value = { "donations/donation_{dId}", "donations/donation_{dId}/" })
	public ResponseEntity<FundraiserDonationDto> getDonationById(@Valid @PathVariable Long dId) {
		return ResponseEntity.ok(this.donationService.getDonationById(dId));
	}

//	by fundraiser
	@GetMapping(value = { "fundraisers/fundraiser_{fId}/donations", "fundraisers/fundraiser_{fId}/donations/" })
	public ResponseEntity<List<FundraiserDonationDto>> getDonationsByFundraiser(@Valid @PathVariable Long fId) {
		return ResponseEntity.ok(this.donationService.getAllDonationsByFundraiser(fId));
	}

//	by donor
	@GetMapping(value = { "users/user_{uId}/donations", "users/user_{uId}/donations/" })
	public ResponseEntity<List<FundraiserDonationDto>> getDonationsByDonor(@Valid @PathVariable Long uId) {
		return ResponseEntity.ok(this.donationService.getAllDonationsByDonor(uId));
	}

	// authenticate user
	public boolean authenticateUser(Long donationId, Long loggedInUserId, Boolean isAdmin)
			throws AccessDeniedException {

		UserDto donor = this.donationService.getDonationById(donationId).getDonor();

		if (donor.getId() == loggedInUserId || isAdmin) {
			return true;
		}
		throw new ApiException("You are not authorized to perform this action.", HttpStatus.FORBIDDEN, false);
	}

}
