package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.List;

public class Endpoint implements Serializable {
	private String key,name;
	private List<String> value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
	public void addValue(String value) {
		this.value.add(value);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
