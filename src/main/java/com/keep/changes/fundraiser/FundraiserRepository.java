package com.keep.changes.fundraiser;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.keep.changes.category.Category;
import com.keep.changes.user.User;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {

	List<Fundraiser> findByEmail(String email);

	List<Fundraiser> findByPhone(String phone);

	List<Fundraiser> findByFundraiserTitleContaining(String keyWord);

	List<Fundraiser> findByCategory(Category category);

	List<Fundraiser> findByPostedBy(User user);
}
