package com.keep.changes.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplateName {

	CONFIRM_EMAIL("confirm-email"), RESET_PASSWORD("reset-password");

	private final String name;

}
