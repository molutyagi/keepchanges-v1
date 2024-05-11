package com.keep.changes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.keep.changes.exception.CustomAccessDeniedHandler;
import com.keep.changes.security.jwt.JwtAuthenticationFilter;
import com.keep.changes.security.jwt.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	private static final String[] ADMIN_ONLY_URLS = { "api/admin/**", "api/categories/**" };

	private static final String[] PUBLIC_URLS = { "/v3/api-docs", "/v2/api-docs", "api/auth/**",
			"/swagger-resources/**", "/swagger-ui/**", "/webjars/**" };

	@Autowired
	private final UserDetailsServiceImpl userDetailsServiceImpl;

	@Autowired
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Autowired
	@Qualifier("delegatedAuthenticationEntryPoint")
	AuthenticationEntryPoint authEntryPoint;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
				.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(this.daoAuthenticationProviderBean())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests((req) -> req.requestMatchers(PUBLIC_URLS).permitAll()
						.requestMatchers(ADMIN_ONLY_URLS).hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "api/users/user/current/**").hasRole("USER")
						.requestMatchers(HttpMethod.GET).permitAll().anyRequest().authenticated())
				.userDetailsService(userDetailsServiceImpl).exceptionHandling(e -> e
						.accessDeniedHandler(this.accessDeniedHandler).authenticationEntryPoint(this.authEntryPoint))
				.httpBasic(Customizer.withDefaults()).build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProviderBean() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(this.userDetailsServiceImpl);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}
}