package com.aptus.blackbox.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.MeteredEndpoints;


public class Metering {
	
	private String userId;
	private int totalRowsFetched;
	private String time,type,connId;
	private Map<String,List<MeteredEndpoints>> rowsFetched= new HashMap<>();
	public int getTotalRowsFetched() {
		return totalRowsFetched;
	}
	public void setTotalRowsFetched(int totalRowsFetched) {
		this.totalRowsFetched = totalRowsFetched;
	}
	public Map<String, List<MeteredEndpoints>> getRowsFetched() {
		return rowsFetched;
	}
//	public void setRowsFetched(Map<String, Integer> rowsFetched) {
//		this.rowsFetched = rowsFetched;
//	}
//	public void setRowsFetched(String endpoint, Integer rows) {
//		this.rowsFetched.put(endpoint, rows);
//	}
	public String getConnId() {
		return connId;
	}
	public void setConnId(String connId) {
		this.connId = connId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setRowsFetched(String category, String label, int rows) {
	
		if(rowsFetched.containsKey(category))
			rowsFetched.get(category).add(new MeteredEndpoints(label,rows));
		else{
			rowsFetched.put(category,new ArrayList<>());
			rowsFetched.get(category).add(new MeteredEndpoints(label, rows));
			
		}

	}
	
}
