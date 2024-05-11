package com.keep.changes.user.token;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {

	private Long id;

	private String token;

	private LocalDateTime createdAt;

	private LocalDateTime expiresAt;

	private LocalDateTime verifiedAt;

	private Boolean verified=false;

	private String email;

}
