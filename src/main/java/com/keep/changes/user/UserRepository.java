package com.keep.changes.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<List<User>> findByEmailContaining(String email);

	Optional<User> findByPhone(String phone);

	Optional<List<User>> findByNameContaining(String name);
}
