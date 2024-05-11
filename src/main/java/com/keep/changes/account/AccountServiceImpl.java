package com.keep.changes.account;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceAlreadyExistsException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private UserRepository userRepository;

	// add
	@Override
	@Transactional
	public AccountDto addAccount(AccountDto accountDto) {

		Optional<Account> accountWithNumber = this.accountRepository.findByAccountNumber(accountDto.getAccountNumber());
		if (accountWithNumber.isPresent()) {
			throw new ResourceAlreadyExistsException("Account", "Number", accountDto.getAccountNumber());
		}

//		get currently loggedinuser
		String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userRepository.findByEmail(loggedInUsername)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", loggedInUsername));

//		convert dto to entity
		Account account = this.modelMapper.map(accountDto, Account.class);
		account.setHoldingEntity(user);

		Account savedAccount = this.accountRepository.save(account);

		return this.modelMapper.map(savedAccount, AccountDto.class);
	}

//	update
//	put
	@Override
	@Transactional
	public AccountDto putUpdateAccount(Long aId, AccountDto ad) {

		Account account = this.accountRepository.findById(aId)
				.orElseThrow(() -> new ResourceNotFoundException("Account", "Id", aId));

		account.putUpdateAccount(aId, ad.getAccountNumber(), ad.getIfsc(), ad.getBankName(), ad.getBranch(),
				ad.getHolderName());

		Account updated = this.accountRepository.save(account);

		return this.modelMapper.map(updated, AccountDto.class);

	}

//	patch
	@Override
	@Transactional
	public AccountDto patchUpdateAccount(Long aId, AccountDto partialAccountDto) {

		Account account = this.accountRepository.findById(aId)
				.orElseThrow(() -> new ResourceNotFoundException("Account", "Id", aId));

		Account partialAccount = this.modelMapper.map(partialAccountDto, Account.class);

		Field[] declaredFields = Account.class.getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {

				Object value = field.get(partialAccount);

				if (value != null) {

					field.set(account, value);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ApiException("Error updating account. Try again!!", HttpStatus.BAD_REQUEST, false);
			}
		}

		Account updated = this.accountRepository.save(account);
		return this.modelMapper.map(updated, AccountDto.class);
	}

//	delete
	@Override
	@Transactional
	public void deleteAccount(Long aId) {

		Account account = this.accountRepository.findById(aId)
				.orElseThrow(() -> new ResourceNotFoundException("Account", "Id", aId));

		this.accountRepository.delete(account);
	}

//	get
//	by Id
	@Override
	@Transactional
	public AccountDto getAccountById(Long aId) {
		Account account = this.accountRepository.findById(aId)
				.orElseThrow(() -> new ResourceNotFoundException("Account", "Id", aId));

		return this.modelMapper.map(account, AccountDto.class);
	}

//	all
	@Override
	@Transactional
	public List<AccountDto> getAllAccounts() {
		List<Account> all = this.accountRepository.findAll();
		List<AccountDto> accountDtos = new ArrayList<>();
		for (Account account : all) {
			AccountDto accountDto = this.modelMapper.map(account, AccountDto.class);
			accountDtos.add(accountDto);
		}
		return accountDtos;
	}

//	by holder
	@Override
	@Transactional
	public List<AccountDto> getAccountByHoldingEntity(Long uId) {

		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		List<Account> byHoldingEntity = this.accountRepository.findByHoldingEntity(user);
		List<AccountDto> accountDtos = new ArrayList<>();

		for (Account account : byHoldingEntity) {
			AccountDto accountDto = this.modelMapper.map(account, AccountDto.class);
			accountDtos.add(accountDto);
		}
		return accountDtos;
	}

//	by account number
	@Override
	public AccountDto getAccountByAccountNumber(String accountNumber) {
		Account account = this.accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Account", "Number", accountNumber));

		return this.modelMapper.map(account, AccountDto.class);
	}

}
