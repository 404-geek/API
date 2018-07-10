package com.aptus.blackbox.datamodels;

import java.util.List;

public class EndPointObj {
	private String category;
	private List<String> endpoints;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public List<String> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}
	
	@Override
	public String toString() {
		return "category:"+category+" endpoints:"+endpoints;
	}
	
	
	
}
