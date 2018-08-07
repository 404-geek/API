package com.aptus.blackbox.datamodels.Scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Connection {
	
	private String status,message;
	private String lastSuccessfullPushed,nextScheduledPushed;
	private Map<String,List<Endpoint>> category;
	
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
	public Map<String,List<Endpoint>> getCategory() {
		return category;
	}
	public void setCategory( String category,Endpoint endpoint) {
		if(this.category.containsKey(category))
				this.category.get(category).add(endpoint);
		else {
				List<Endpoint> list = new ArrayList<>();
				list.add(endpoint);
				this.category.put(category, list);
			}
	}
	
	
	
	
}
