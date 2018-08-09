package com.aptus.blackbox.datamodels.Scheduling;

import java.util.HashMap;
import java.util.Map;

public class Endpoint {
	private Map<String,StatusObj> endpoints=new HashMap<>();

	public Map<String,StatusObj> getEndpoints() {
		return endpoints;
	}

	
	public void setEndpoints(String endpoint,StatusObj status) {
		this.endpoints.put(endpoint, status);
	}
	
}
