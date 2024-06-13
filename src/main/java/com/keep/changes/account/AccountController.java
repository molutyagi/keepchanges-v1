package com.keep.changes.account;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keep.changes.exception.ApiException;
import com.keep.changes.fundraiser.FundraiserDto;
import com.keep.changes.fundraiser.FundraiserService;
import com.keep.changes.payload.response.ApiResponse;
import com.keep.changes.user.UserDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {

	@Autowired
	private AccountService accountService;

	@Autowired
	private FundraiserService fundraiserService;

	@Autowired
	private ObjectMapper objectMapper;

//	Add account
	@PostMapping(value = { "add", "add/" })
	public ResponseEntity<AccountDto> addAccount(@Valid @RequestBody AccountDto accountDto) {

		return new ResponseEntity<AccountDto>(this.accountService.addAccount(accountDto), HttpStatus.CREATED);
	}

//	Add Account from fundraiser
	@PostMapping(value = { "fundraiser/account", "fundraiser/account/" })
	public ResponseEntity<?> addFundraiserAccount(@Valid @RequestParam(value = "fId", required = false) Long fId,
			@RequestParam(value = "accountData", required = true) String accountData) {

		AccountDto accountDto;

		try {
			accountDto = this.objectMapper.readValue(accountData, AccountDto.class);
		} catch (JsonProcessingException e) {
			throw new ApiException("Invalid Request Data! Kindly enter valid data.", HttpStatus.BAD_REQUEST, false);
		}

		FundraiserDto fundraiserDto = this.fundraiserService.getFundraiserById(fId);
		Set<FundraiserDto> fundraiserDtos = new HashSet<>();
		fundraiserDtos.add(fundraiserDto);
//		accountDto.setAssociatedFundraiser(fundraiserDtos);

		return new ResponseEntity<>(this.accountService.addAccount(accountDto), HttpStatus.CREATED);
	}

//	update
//	put update
	@PutMapping(value = { "account_{aId}", "account_{aId}/" })
	@PreAuthorize("@accountController.authenticateUser(#aId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<AccountDto> putUpdateAccount(@Valid @PathVariable Long aId,
			@RequestBody AccountDto accountDto) {

		return ResponseEntity.ok(this.accountService.putUpdateAccount(aId, accountDto));
	}

//	patch update
	@PatchMapping(value = { "account_{aId}", "account_{aId}/" })
	@PreAuthorize("@accountController.authenticateUser(#aId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<AccountDto> patchUpdateAccount(@Valid @PathVariable Long aId,
			@RequestBody AccountDto partialAccount) {

		return ResponseEntity.ok(this.accountService.patchUpdateAccount(aId, partialAccount));
	}

//	delete 
	@DeleteMapping(value = { "account_{aId}", "account_{aId}/" })
	@PreAuthorize("@accountController.authenticateUser(#aId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deletAccount(@PathVariable Long aId) {

		this.accountService.deleteAccount(aId);

		return ResponseEntity.ok(new ApiResponse("Account deleted successfully", true));
	}

//	Get
//	get all
	@GetMapping(value = { "getall", "getall/", "", "/" })
	public ResponseEntity<List<AccountDto>> getAllAccounts() {

		return ResponseEntity.ok(this.accountService.getAllAccounts());
	}

//	by Id
	@GetMapping(value = { "account_{aId}", "account_{aId}/" })
	public ResponseEntity<AccountDto> getAccountById(@PathVariable Long aId) {

		return ResponseEntity.ok(this.accountService.getAccountById(aId));
	}

//	by holding entity
	@GetMapping(value = { "account/user_{uId}", "account/user_{uId}/" })
	public ResponseEntity<List<AccountDto>> getAccountByUser(@PathVariable Long uId) {

		return ResponseEntity.ok(this.accountService.getAccountByHoldingEntity(uId));
	}

//	by associated fundraiser
	@GetMapping(value = { "account/fundraiser_{fId}", "account/fundraiser_{fId}/" })
	public ResponseEntity<AccountDto> getFundraiserAccount(@PathVariable Long fId) {

		return ResponseEntity.ok(this.accountService.getFundraiserAccount(fId));
	}

//	by account number
	@GetMapping(value = { "account/number_{aNumber}", "account/number_{aNumber}/" })
	public ResponseEntity<AccountDto> getAccountByAccountNumber(@PathVariable String aNumber) {

		return ResponseEntity.ok(this.accountService.getAccountByAccountNumber(aNumber));
	}

	public boolean authenticateUser(long aId, long cUId, boolean bool) throws AccessDeniedException {
		UserDto holdingEntity = this.accountService.getAccountById(aId).getHoldingEntity();
		if (holdingEntity.getId() == cUId || bool) {
			return true;
		}
		throw new ApiException("You are not authorized to perform this action.", HttpStatus.FORBIDDEN, false);
	}

}
