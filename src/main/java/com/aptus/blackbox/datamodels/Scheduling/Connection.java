package com.aptus.blackbox.datamodels.Scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection {
	
	private String status,message;
	private String lastSuccessfullPushed,nextScheduledPushed;
	private Map<String,List<Map<String,StatusObj>>> category;
	///////////<category,<        endpoint,status>>//////
	
	public Connection() {
		super();
		this.category = new HashMap<>();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getLastSuccessfullPushed() {
		return lastSuccessfullPushed;
	}
	public void setLastSuccessfullPushed(String lastSuccessfullPushed) {
		this.lastSuccessfullPushed = lastSuccessfullPushed;
	}
	public String getNextScheduledPushed() {
		return nextScheduledPushed;
	}
	public void setNextScheduledPushed(String nextScheduledPushed) {
		this.nextScheduledPushed = nextScheduledPushed;
	}
	
	public Map<String, List<Map<String, StatusObj>>> getCategory() {
		return category;
	}
	public void setCategory(String catgeoryName, Map<String, StatusObj> endpointStatus) {
	
		if(this.category.containsKey(catgeoryName))
				this.category.get(catgeoryName).add(endpointStatus);
		else {
				List<Map<String,StatusObj>> list = new ArrayList<>();
				list.add(endpointStatus);
				this.category.put(catgeoryName, list);
			}
	}
	
	
	
	
}