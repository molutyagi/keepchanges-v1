package com.keep.changes.test.faker;

import java.util.List;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;
import com.keep.changes.category.Category;
import com.keep.changes.category.CategoryRepository;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.fundraiser.AdminApproval;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.fundraiser.FundraiserRepository;
import com.keep.changes.fundraiser.FundraiserStatus;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SampleDataFundraiser {

	private final FundraiserRepository fundraiserRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;

	private static final Faker faker = new Faker();
	private static final int BATCH_SIZE = 50; // Adjust batch size as needed
	private static final Long MIN_ID = (long) 1;
	private static final Long MAX_ID = (long) 100;

	private static Long nextId = MIN_ID; // Track the next ID to be assigned

	private Long uId = 1L;
	private Long cId = 1L;

	@Transactional
	public void generateAndPersistFundraisers(EntityManager entityManager) {

		List<Fundraiser> fundraiserBatch = new ArrayList<>();
		User user = this.getUser(uId);
		Category category = this.getCategory(cId);

		for (int i = 0; i < BATCH_SIZE; i++) {

			if (nextId % 25 == 0) {
				uId++;
				cId++;
				user = this.getUser(uId);
				category = this.getCategory(cId);
			}
			fundraiserBatch.add(generateRandomFundraiser(getNextId(), user, category));
		}

		fundraiserRepository.saveAll(fundraiserBatch);

//	        // Optionally persist additional batches
//	        while (/* condition to check for more data to generate */) {
//	            fundraiserBatch = new ArrayList<>();
//	            // ... (logic to generate another batch of Fundraiser objects)
//	            persistFundraisers(entityManager, fundraiserBatch);
//	        }
	}

	private static synchronized Long getNextId() {
		if (nextId > MAX_ID) {
			throw new IllegalStateException("Reached maximum ID limit: " + MAX_ID);
		}
		return nextId++;
	}

	private Fundraiser generateRandomFundraiser(Long id, User user, Category category) {
		Fundraiser fundraiser = new Fundraiser();

		if (id % 2 == 0) {
			fundraiser.setApproval(AdminApproval.APPROVED); // Or other approval states
			fundraiser.setAdminRemarks("admin reviewd and accepted");
			fundraiser.setIsReviewed(true);

			fundraiser.setIsActive(true);
			fundraiser.setStatus(FundraiserStatus.OPEN); // Or other fundraiser statuses
			if (id % 4 == 0) {
				fundraiser.setStatus(FundraiserStatus.COMPLETED); // Or other fundraiser statuses
				fundraiser.setIsActive(false);
			}
		} else {
			fundraiser.setApproval(AdminApproval.PENDING); // Or other approval states
			fundraiser.setAdminRemarks("");
			fundraiser.setIsReviewed(false);

			fundraiser.setIsActive(false);
			fundraiser.setStatus(FundraiserStatus.INACTIVE); // Or other fundraiser statuses

			if (id % 5 == 0) {
				fundraiser.setStatus(FundraiserStatus.CLOSED); // Or other fundraiser statuses
			}
			if (id % 7 == 0) {
				fundraiser.setApproval(AdminApproval.DISAPPROVED); // Or other approval states
				fundraiser.setStatus(FundraiserStatus.CANCELLED); // Or other fundraiser statuses
				fundraiser.setAdminRemarks("disapproved");
				fundraiser.setIsReviewed(true);
			}

		}

		// Set ID
		fundraiser.setId(id);

		// Basic Details
		fundraiser.setFundraiserTitle(faker.company().name());
		fundraiser.setFundraiserDescription(faker.lorem().paragraph(2));
		fundraiser.setBeneficiary(faker.company().name());
		fundraiser.setRaiseGoal(1500000.0);
		fundraiser.setRaised(15000.0);
		fundraiser.setEmail(faker.internet().emailAddress());
		fundraiser.setPhone(faker.phoneNumber().phoneNumber());

		// Dates
		fundraiser.setStartDate(faker.date().past(30, TimeUnit.DAYS));
		fundraiser.setEndDate(faker.date().future(30, TimeUnit.DAYS));

		// Other Details
		fundraiser.setDisplayPhoto("https://res.cloudinary.com/dv6rzh2cp/image/upload/v1715067696/Group_4_wfvpsb.png");
//		fundraiser.setIsActive(faker.bool().bool());
//		fundraiser.setApproval(AdminApproval.APPROVED); // Or other approval states
//		fundraiser.setAdminRemarks("admin reviewd and accepted");
//		fundraiser.setIsReviewed(true);
//		fundraiser.setStatus(FundraiserStatus.OPEN); // Or other fundraiser statuses

		// Relationships (replace with your generation logic)
		// Category
		fundraiser.setCategory(category); // Generate or reference existing category

		// User
		fundraiser.setPostedBy(user); // Generate or reference existing user

		// Address
//		fundraiser.setAddress(generateRandomAddress()); // Implement generateRandomAddress()
//
//		// Account (assuming basic account details)
//		fundraiser.setAccount(generateRandomAccount()); // Implement generateRandomAccount()

		// Other relationships (implement similar methods for Transaction, Donation
		// etc.)
		fundraiser.setTransactions(new HashSet<>());
		fundraiser.setDonations(new HashSet<>());

		return fundraiser;
	}

	private User getUser(Long uId) {
		return this.userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));
	}

	private Category getCategory(Long cId) {
		return this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));
	}
}
