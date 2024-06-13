package com.keep.changes.admin.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.stereotype.Component;

@Endpoint(id = "custom")
@Component
public class CustomActuatorEndpoint {

	public Object customEndpoint() {
		Map<String, String> map = new HashMap<>();
		map.put("key", "value");

		return map;
	}

}
