package com.keep.changes.fundraiser;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.keep.changes.category.Category;
import com.keep.changes.user.User;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {

	@Query("SELECT f FROM Fundraiser f WHERE f.isActive=true")
	List<Fundraiser> findAllActiveFundraisers();

	List<Fundraiser> findByEmail(String email);

	List<Fundraiser> findByPhone(String phone);

	List<Fundraiser> findByFundraiserTitleContaining(String keyWord);

	@Query("SELECT f FROM Fundraiser f WHERE f.category= :category AND isActive=true")
	List<Fundraiser> findByCategory(@Param("category") Category category);

	List<Fundraiser> findByPostedBy(User user);

	@Query("SELECT f FROM Fundraiser f WHERE f.isActive=true ORDER BY f.id DESC LIMIT 6")
	List<Fundraiser> findLatestFundraisers();

	@Query("SELECT f FROM Fundraiser f WHERE f.postedBy= :user AND f.isActive=true")
	List<Fundraiser> findActiveByPostedBy(@Param("user") User user);

//	admin dashboard 
	@Query("SELECT SUM(f.raised) FROM Fundraiser f")
	Double sumOfRaised();

	@Query("SELECT SUM(f.raiseGoal) FROM Fundraiser f")
	Double sumOfRaiseGoal();

	Long countAllByIsActive(Boolean isActive);

	List<Fundraiser> findByIsReviewedFalse();

}
