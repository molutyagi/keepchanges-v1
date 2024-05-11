package com.keep.changes;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.keep.changes.config.AppConstants;
import com.keep.changes.role.Role;
import com.keep.changes.role.RoleRepository;

@SpringBootApplication
@EnableAsync
public class KeepchangesV1Application implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(KeepchangesV1Application.class, args);
	}

    @Bean
    ModelMapper modelMapper() {

		return new ModelMapper();
	}

	@Override
	public void run(String... args) throws Exception {

		try {
			Role admin = new Role(AppConstants.ADMIN_USER, "ROLE_ADMIN", "Admin");
			Role user = new Role(AppConstants.NORMAL_USER, "ROLE_USER", "User");

			List<Role> roles = List.of(admin, user);

			this.roleRepository.saveAll(roles);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
