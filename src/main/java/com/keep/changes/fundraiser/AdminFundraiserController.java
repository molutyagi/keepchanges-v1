package com.keep.changes.fundraiser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.exception.ApiException;
import com.keep.changes.payload.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("admin/fundraisers")
public class AdminFundraiserController {

	@Autowired
	private FundraiserService fundraiserService;

	@PatchMapping(value = { "fundraiser_{fId}", "fundraiser_{fId}/" })
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> fundraiserAdminController(@Valid @PathVariable Long fId,
			@RequestParam(value = "adminRemarks", required = false) String adminRemarks,
			@RequestParam(value = "adminStatus", required = false) AdminApproval adminStatus) {

		try {
			this.fundraiserService.fundraiserAdminService(fId, adminRemarks, adminStatus);
		} catch (Exception e) {
			throw new ApiException("An error occured while changing fundraiser status. Try again.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
		return ResponseEntity.ok(new ApiResponse("Fundraiser status changed successfully.", true));

	}

}
