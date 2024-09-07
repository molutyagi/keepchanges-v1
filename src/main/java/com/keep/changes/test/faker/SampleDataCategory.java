package com.keep.changes.test.faker;

import com.github.javafaker.Faker;
import com.keep.changes.category.Category;
import com.keep.changes.category.CategoryRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleDataCategory {

	private final CategoryRepository categoryRepository;
	private static final Faker faker = new Faker();
	private static final int BATCH_SIZE = 20; // Adjust batch size as needed
	private static final Long MIN_ID = (long) 1;
	private static final Long MAX_ID = (long) 50;

	private static Long nextId = MIN_ID; // Track the next ID to be assigned

	@Transactional
	public void generateAndPersistCategories(EntityManager entityManager) {
		List<Category> categoryBatch = new ArrayList<>();
		for (int i = 0; i < BATCH_SIZE; i++) {
			categoryBatch.add(generateRandomCategory(getNextId()));
		}

		categoryRepository.saveAll(categoryBatch);
	}

	private static Category generateRandomCategory(Long id) {
		Category category = new Category();

		category.setId(id);
		category.setCategoryName("Category: " + id);
		category.setCategoryDescription(faker.lorem().paragraph(2));
		category.setCategorySvg("deault.png"); // Replace with image generation logic if needed

		return category;
	}

	private static synchronized Long getNextId() {
		if (nextId > MAX_ID) {
			throw new IllegalStateException("Reached maximum ID limit: " + MAX_ID);
		}
		return nextId++;
	}
}
