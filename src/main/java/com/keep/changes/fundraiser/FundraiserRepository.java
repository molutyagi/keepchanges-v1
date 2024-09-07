package com.keep.changes.fundraiser;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.keep.changes.category.Category;
import com.keep.changes.user.User;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {

	Page<Fundraiser> findAllByIsActiveTrue(Pageable pageable);

	List<Fundraiser> findAllByIsActiveTrue();

	Page<Fundraiser> findAllByIsActiveTrueAndEmailContaining(String email, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndPhoneContaining(String phone, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndFundraiserTitleContaining(String keyWord, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndCategory(Category category, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndCategoryIn(List<Category> category, Pageable pageable);

	Page<Fundraiser> findByPostedBy(User user, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndPostedByIn(List<User> user, Pageable pageable);

	Page<Fundraiser> findAllByIsActiveTrueAndPostedBy(User user, Pageable pageable);

	Page<Fundraiser> findTop6ByIsActiveTrueOrderByIdDesc(Pageable pageable);

	Page<Fundraiser> findByIsReviewedFalse(Pageable pageable);

//	admin dashboard 
	@Query("SELECT SUM(raised) FROM Fundraiser f WHERE f.isActive = true")
	Double sumRaisedByIsActive();

	@Query("SELECT SUM(f.raiseGoal) FROM Fundraiser f")
	Double sumOfRaiseGoal();

	Long countAllByIsActive(Boolean isActive);

}
