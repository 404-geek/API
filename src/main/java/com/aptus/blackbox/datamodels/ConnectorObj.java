package com.aptus.blackbox.datamodels;

import java.util.List;

public class ConnectorObj extends EndPointObj{

	private String sourceName, destName, connectionId, scheduled, period;
	private List<EndPointObj> endPointObjs;
	
	public List<EndPointObj> getEndpointObjs() {
		return endPointObjs;
	}
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
	public String getScheduled() {
		return scheduled;
	}
	public void setScheduled(String scheduled) {
		this.scheduled = scheduled;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	
	@Override
	public String toString() {
		return "sourceName:"+sourceName+"  destName:"+destName+
				"  connectionId:"+connectionId+"  scheduled:"+scheduled+
				"  period:"+period+"  endPointObjs:"+endPointObjs;
	}
	
}
