package com.keep.changes.account;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.keep.changes.user.User;
import com.keep.changes.fundraiser.Fundraiser;

public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findByHoldingEntity(User holdingEntity);

	Optional<Account> findByAssociatedFundraisers(Fundraiser associatedFundraiser);

	Optional<Account> findByAccountNumber(String accountNumber);
}
