
package com.keep.changes.user.token;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

	Optional<Token> findByToken(String token);

	@Query("SELECT t FROM Token t WHERE t.token = :tokenValue AND t.email = :email ORDER BY t.id DESC")
	Optional<Token> findLatestTokenByEmailAndToken(@Param("email") String email,
			@Param("tokenValue") String tokenValue);

}
