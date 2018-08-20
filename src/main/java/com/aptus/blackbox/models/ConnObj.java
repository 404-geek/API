package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConnObj implements Serializable {
	
	private String sourceName,destName,connectionId,scheduled;
	private String sourceId, destinationId;
	
	private long period;
	private List<Endpoint> endPoints=new ArrayList<Endpoint>();
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
	public List<Endpoint> getEndPoints() {
		return endPoints;
	}
	public void setEndPoints(Endpoint endPoint) {
		this.endPoints.add(endPoint);
	}
	public void setEndPoints(List<Endpoint> endPoint) {
		this.endPoints.clear();
		this.endPoints.addAll(endPoint);
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
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getDestinationId() {
		return destinationId;
	}
	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}
	
}
