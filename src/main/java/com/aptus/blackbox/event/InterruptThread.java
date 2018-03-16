package com.aptus.blackbox.event;

import java.util.concurrent.ScheduledFuture;

public class InterruptThread {
	private boolean userInterrupted;
	private ScheduledFuture<?> thread;
	private String userId,connectionId;
	public ScheduledFuture<?> getThread() {
		return thread;
	}

	public InterruptThread(ScheduledFuture<?> thread,boolean userInterrupted,String userId,String connectionId) {
		this.thread = thread;
		this.userInterrupted = userInterrupted;
		this.userId = userId;
		this.connectionId = connectionId;
	}

	public boolean isUserInterrupted() {
		return userInterrupted;
	}

	public String getUserId() {
		return userId;
	}

	public String getConnectionId() {
		return connectionId;
	}
}
