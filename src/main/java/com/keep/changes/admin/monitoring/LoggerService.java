package com.keep.changes.admin.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LoggerService implements HealthIndicator {

	private final String LOGGER_SERVICE = "LOGGER Service";

	@Override
	public Health health() {
		if (isLoggerServiceGood()) {
			return Health.up().withDetail(LOGGER_SERVICE, "LOGGER Service is up and running").build();
		}
		return Health.down().withDetail(LOGGER_SERVICE, "LOGGER Service is down and not running").build();
	}

	private boolean isLoggerServiceGood() {
		return false;
	}
}
