package com.keep.changes.payload.response;

import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse {
	public ApiResponse(String msg2, CompletableFuture<Boolean> isVerified) {
		// TODO Auto-generated constructor stub
	}
	private String msg;
	private boolean success;
}
