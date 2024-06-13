package com.keep.changes.auth;

import java.io.IOException;

import javax.naming.AuthenticationException;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.keep.changes.user.UserDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

	AuthenticationResponse register(UserDto requestUser);

	AuthenticationResponse login(AuthenticationRequest userRequest) throws AuthenticationException;

	void refreshToken(HttpServletRequest request, HttpServletResponse response) throws StreamWriteException, DatabindException, IOException;

	AuthenticationResponse resetPassword(AuthenticationRequest authRequest);

}
