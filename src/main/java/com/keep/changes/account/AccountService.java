package com.keep.changes.account;

import java.util.List;

public interface AccountService {

//	add account
	AccountDto addAccount(AccountDto accountDto);

//	update
//	put
	AccountDto putUpdateAccount(Long aId, AccountDto accountDto);

//	patch
	AccountDto patchUpdateAccount(Long aId, AccountDto partialAccount);

//	delete
	void deleteAccount(Long aId);

//	get
//	by Id
	AccountDto getAccountById(Long aId);

//	get all
	List<AccountDto> getAllAccounts();

//	get by Holder
	List<AccountDto> getAccountByHoldingEntity(Long uId);

	AccountDto getAccountByAccountNumber(String accountNumber);

	AccountDto getFundraiserAccount(Long fId);

}
