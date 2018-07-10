package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class SourceCredentials {

	@Id
	private String _id;
	private List<Pairs> credentials;
	
	public SourceCredentials() {
		setCredentials(new ArrayList<Pairs>());
	}
	
	public List<Pairs> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<Pairs> credentials) {
		this.credentials = credentials;
	}
	
}

class Pairs{
	
	private String key;
	private String value;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
