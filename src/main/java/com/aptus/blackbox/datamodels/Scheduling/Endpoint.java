package com.aptus.blackbox.datamodels.Scheduling;

import java.util.HashMap;
import java.util.Map;

public class Endpoint {
	private Map<String,Status> endpoints=new HashMap<>();

	public Map<String,Status> getEndpoints() {
		return endpoints;
	}

	
	public void setEndpoints(String endpoint,Status status) {
		this.endpoints.put(endpoint, status);
	}
	
}
