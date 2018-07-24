package com.aptus.blackbox.event;

import java.util.HashMap;
import java.util.Map;

import com.aptus.blackbox.models.MeteredEndpoints;

public class Metering {
	private int totalRowsFetched;
	private String connId,time,type,userId;
	private Map<String,MeteredEndpoints> rowsFetched= new HashMap<>();
	public int getTotalRowsFetched() {
		return totalRowsFetched;
	}
	public void setTotalRowsFetched(int totalRowsFetched) {
		this.totalRowsFetched = totalRowsFetched;
	}
	public Map<String, MeteredEndpoints> getRowsFetched() {
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

	public void setRowsFetched(String catagory, String label, int rows) {
		// TODO Auto-generated method stub
		
	}
	
}
