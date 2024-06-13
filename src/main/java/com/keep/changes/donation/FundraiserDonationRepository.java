package com.keep.changes.donation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.user.User;

@Repository
public interface FundraiserDonationRepository extends JpaRepository<FundraiserDonation, Long> {

	List<FundraiserDonation> findByFundraiser(Fundraiser fundraiser);

	List<FundraiserDonation> findByDonor(User user);

	@Query("SELECT COUNT(DISTINCT d.donor) FROM FundraiserDonation d")
	Long totalDonors();
	
	@Query("SELECT COUNT(DISTINCT d.fundraiser) FROM FundraiserDonation d")
	Long totalDonatedFundraisers();
}
