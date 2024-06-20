package com.keep.changes.test.faker;

import com.github.javafaker.Faker;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.role.Role;
import com.keep.changes.role.RoleRepository;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleDataUser {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private static final Faker faker = new Faker();
	private static final int BATCH_SIZE = 2000; // Adjust batch size as needed
	private static final Long MIN_ID = (long) 2;
	private static final Long MAX_ID = (long) 3000;

	private static Long nextId = MIN_ID; // Track the next ID to be assigned

	@Transactional
	public void generateAndPersistUsers(EntityManager entityManager) {
		List<User> userBatch = new ArrayList<>();
		Set<Role> roleSet = new HashSet<>();
		roleSet.add(
				this.roleRepository.findById(502L).orElseThrow(() -> new ResourceNotFoundException("Role", "Id", 502)));

		for (int i = 0; i < BATCH_SIZE; i++) {
			userBatch.add(generateRandomUser(getNextId(), roleSet));
		}

		userRepository.saveAll(userBatch);
	}

	private User generateRandomUser(Long id, Set<Role> role) {
		User user = new User();

		user.setId(id);
		user.setName(faker.name().fullName());
		user.setEmail(faker.internet().emailAddress());
		user.setPassword(this.passwordEncoder.encode("Abcd@1234"));
		user.setPhone(faker.phoneNumber().cellPhone());
		user.setDisplayImage("https://res.cloudinary.com/dv6rzh2cp/image/upload/v1715067696/Group_4_wfvpsb.png");
		user.setAbout(faker.lorem().paragraph(2)); // Generate a short paragraph
		user.setRegisterTime(new java.util.Date()); // Set current timestamp

		// Set default values for other fields
		user.setIsEnabled(true);
		user.setRoles(role); // Initialize empty set for roles

		return user;
	}

	private static synchronized Long getNextId() {
		if (nextId > MAX_ID) {
			throw new IllegalStateException("Reached maximum ID limit: " + MAX_ID);
		}
		return nextId++;
	}
}
