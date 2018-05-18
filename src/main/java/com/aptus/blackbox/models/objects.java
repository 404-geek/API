package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.List;

public class objects implements Serializable{
	private String key,value;
	private List<objects> valueList;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public List<objects> getValueList() {
		return valueList;
	}
	public void setValueList(List<objects> valueList) {
		this.valueList = valueList;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return "Key="+this.key+" Value="+value+" ValueList="+valueList; 
	}
	
}
