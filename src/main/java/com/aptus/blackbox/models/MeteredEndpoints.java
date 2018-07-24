package com.aptus.blackbox.models;

public class MeteredEndpoints {
	private String endpoint;
	private  int numRecords;
	
	public MeteredEndpoints(String endpoint, int numRecords) {
		this.endpoint = endpoint;
		this.numRecords = numRecords;
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public long getNumRecords() {
		return numRecords;
	}
	public void setNumRecords(int numRecords) {
		this.numRecords = numRecords;
	}
}
