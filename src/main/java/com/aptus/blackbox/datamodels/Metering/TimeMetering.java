package com.aptus.blackbox.datamodels.Metering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class TimeMetering {

	private int totalRows;
	private String type,time;
	private Map<String,List<EndpointMetering>> endpoints= new HashMap<>();
	
	public int getTotalRows() {
		return totalRows;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Map<String, List<EndpointMetering>> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(String category,EndpointMetering endpoint) {
		if(this.endpoints.containsKey(category))
			this.endpoints.get(category).add(endpoint);
		else {
			this.endpoints.put(category, new ArrayList<>());
			this.endpoints.get(category).add(endpoint);
		}
	}
	public void addTotalRows(int totalRows) {
		this.totalRows = this.totalRows+totalRows;
	}
	
	public void setTotalRows(int rows) {
		this.totalRows =rows;
		
	}

	


}
