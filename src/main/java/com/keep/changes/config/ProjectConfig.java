package com.keep.changes.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class ProjectConfig {

	@Bean
	public Cloudinary getCloudinary() {
		Map<Object, Object> config = new HashMap<>();

		config.put("cloud_name", "didpgi5h3");
		config.put("api_key", "594882277617552");
		config.put("api_secret", "okHLvngK7D8ioXB2bHZt-hTBUSU");
		config.put("secure", true);

		return new Cloudinary(config);
	}

}
