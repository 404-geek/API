package com.aptus.blackbox.index;

import java.io.Serializable;
import java.util.List;

public class ConnObj implements Serializable {
	private String sourceName,destName,connectionId;
	private List<String> endPoints;
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
	public void setEndPoints(List<String> endPoints) {
		this.endPoints = endPoints;
	}
	@Override
	public String toString() {
		System.out.println();
		return this.sourceName+" "+this.destName+" "+this.connectionId+" "+this.endPoints;
	}
	
}
