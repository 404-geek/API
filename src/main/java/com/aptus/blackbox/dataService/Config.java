package com.aptus.blackbox.dataService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Config {
	
	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${app.mode}")
	private String appMode;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	
	public  String getAppMode() {
		return appMode;
	}
	public String getMongoUrl() {
		return mongoUrl;
	}
	
	public void setMongoUrl(String mongoUrl) {
		this.mongoUrl= mongoUrl;
	}
	
	public String getRootUrl() {
		return rootUrl;
	}
	
}
