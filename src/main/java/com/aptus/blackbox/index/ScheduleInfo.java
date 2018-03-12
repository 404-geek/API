package com.aptus.blackbox.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

public class ScheduleInfo implements Serializable {
	private long lastAccessTime;
	private Map<String,SchedulingObjects> schedulingObjects = new HashMap<String,SchedulingObjects>();
	public Map<String,SchedulingObjects> getSchedulingObjects() {
		return schedulingObjects;
	}
	public void setSchedulingObjects(Map<String,SchedulingObjects> schedulingObjects) {
		this.schedulingObjects.putAll(schedulingObjects);
	}
	public void setSchedulingObjects(SchedulingObjects schedulingObjects,String connectionId) {
		this.schedulingObjects.put(connectionId, schedulingObjects);
	}
	public void unSetSchedulingObjects(String connectionId) {
		this.schedulingObjects.remove(connectionId);
	}
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
}
