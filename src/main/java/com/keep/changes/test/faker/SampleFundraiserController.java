package com.keep.changes.test.faker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("sample-data")
@RequiredArgsConstructor
public class SampleFundraiserController {

	private final SampleDataFundraiser sampleDataFundraiser;
	private final SampleDataCategory sampleDataCategory;
	private final SampleDataUser sampleDataUser;

	@PersistenceContext
	private EntityManager entityManager;

	@GetMapping("fundraiser")
	public String sampleFundraiser() {
		this.sampleDataFundraiser.generateAndPersistFundraisers(entityManager);
		return "chal to gya h";
	}

	@GetMapping("category")
	public String sampleCategory() {
		this.sampleDataCategory.generateAndPersistCategories(entityManager);
		;
		return "chal to gya h";
	}

	@GetMapping("user")
	public String sampleUser() {
		this.sampleDataUser.generateAndPersistUsers(entityManager);
		;
		return "chal to gya h";
	}

	@GetMapping("isTrue")
	public boolean isTrue() {
		Long id = 4L;
		if (id % 2 == 0) {
			return true;
		}
		return false;
	}

}
