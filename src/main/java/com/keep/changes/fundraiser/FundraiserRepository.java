package com.keep.changes.fundraiser;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.keep.changes.category.CategoryDto;
import com.keep.changes.user.UserDto;

@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {

	List<Fundraiser> findByEmail(String email);

	List<Fundraiser> findByPhone(String phone);

	List<Fundraiser> findByFundraiserTitleContaining(String keyWord);

	List<Fundraiser> findByCategory(CategoryDto categoryDto);

	List<Fundraiser> findByPostedBy(UserDto userDto);

	List<Fundraiser> findByCauseContaining(String cause);
}
