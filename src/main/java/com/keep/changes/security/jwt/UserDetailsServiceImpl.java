package com.keep.changes.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) {
		System.out.println(username + " : username");
		
		return this.userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException(username));
	}

}
