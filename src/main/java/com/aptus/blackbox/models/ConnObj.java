package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConnObj implements Serializable {
	private String sourceName,destName,connectionId,scheduled;
	private long period;
	private List<String> endPoints=new ArrayList<String>();
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getDestName() {
		return destName;
	}
	public void setDestName(String destName) {
		this.destName = destName;
	}
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	public List<String> getEndPoints() {
		return endPoints;
	}
	public void setEndPoints(String endPoint) {
		this.endPoints.add(endPoint);
	}
	public void setEndPoints(List<String> endPoint) {
		this.endPoints.clear();
		this.endPoints.addAll(endPoint);
	}
	@Override
	public String toString() {
		System.out.println();
		return this.sourceName+" "+this.destName+" "+this.connectionId+" "+this.endPoints;
	}
	public String getScheduled() {
		return scheduled;
	}
	public void setScheduled(String scheduled) {
		this.scheduled = scheduled;
	}
	public long getPeriod() {
		return period;
	}
	public void setPeriod(long period) {
		this.period = period;
	}
	
}
