package com.aptus.blackbox.event;

public class ScheduleEventData {

	private String userId,connId;

	public String getUserId() {
		return userId;
	}

	public void setData(String userId,String connId) {
		this.userId = userId;
		this.connId=connId;
	}

	public String getConnId() {
		return connId;
	}


}
