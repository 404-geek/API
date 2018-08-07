package com.aptus.blackbox.datamodels.Scheduling;

import java.util.HashMap;
import java.util.Map;

public class Endpoint {
	private Map<String,Status> endpoints=new HashMap<>();

	public Map<String,Status> getEndpoints() {
		return endpoints;
	}

	
	public void setEndpoints(String endpoint,String statusCode,String statusMsg) {
	
		Status status = new Status();
		status.setCode(statusCode);
		status.setMessage(statusMsg);
		this.endpoints.put(endpoint, status);
	}
	
	
}
