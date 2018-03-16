package com.aptus.blackbox.event;

public class PostExecutorComplete {
	String userId,connectionId;

	public PostExecutorComplete(String userId,String connectionId){
		this.userId=userId;
		this.connectionId = connectionId;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	
}
