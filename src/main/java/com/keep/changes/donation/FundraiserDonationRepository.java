package com.keep.changes.donation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.user.User;

@Repository
public interface FundraiserDonationRepository extends JpaRepository<FundraiserDonation, Long> {

	List<FundraiserDonation> findByFundraiser(Fundraiser fundraiser);

	List<FundraiserDonation> findByDonor(User user);
}
