package com.aptus.blackbox.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.datamodels.Metering.TimeMetering;
import com.aptus.blackbox.event.Metering;

public class SchedulingObjects implements Serializable{
	private boolean SrcValid,DestValid;
	private String sourceId,destinationId;
	private String status,message,DestName,SrcName;
	private long lastPushed,nextPush,period;
	private Map<String,Map<String,Status>>endPointStatus = new HashMap<String,Map<String,Status>>();
	//OLDprivate Metering metering = new Metering();
	private TimeMetering timeMetering;
	private int totalRows;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private SourceConfig SrcObj;
	private DestinationConfig DestObj;
	private ScheduledFuture<?> thread;
	
	public SchedulingObjects() {
		setTimeMetering(new TimeMetering());
	}
	
	public boolean isSrcValid() {
		return SrcValid;
	}
	public void setSrcValid(boolean srcValid) {
		SrcValid = srcValid;
	}
	public boolean isDestValid() {
		return DestValid;
	}
	public void setDestValid(boolean destValid) {
		DestValid = destValid;
	}
	public Map<String, String> getSrcToken() {
		return SrcToken;
	}
	public void setSrcToken(Map<String, String> srcToken) {
		SrcToken = srcToken;
	}
	public void setSrcToken(String key, String srcToken) {
		SrcToken.put(key, srcToken);
	}
	public Map<String, String> getDestToken() {
		return DestToken;
	}
	public void setDestToken(Map<String, String> destToken) {
		DestToken = destToken;
	}
	public SourceConfig getSrcObj() {
		return SrcObj;
	}
	public void setSrcObj(SourceConfig srcObj) {
		SrcObj = srcObj;
	}
	public DestinationConfig getDestObj() {
		return DestObj;
	}
	public void setDestObj(DestinationConfig destObj) {
		DestObj = destObj;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getLastPushed() {
		return lastPushed;
	}
	public void setLastPushed(long lastPushed) {
		this.lastPushed = lastPushed;
	}
	public long getNextPush() {
		return nextPush;
	}
	public void setNextPush(long nextPush) {
		this.nextPush = nextPush;
	}
	public Map<String, Map<String, Status>> getEndPointStatus() {
		return endPointStatus;
	}
	public List<String> getstatus() {
		List<String> ret = new ArrayList<String>();		
		this.endPointStatus.values().iterator()
		.forEachRemaining(col -> col.values().iterator().forEachRemaining(cal -> ret.add(cal.getStatus())));		
		return ret;
	}
	public void setEndPointStatus(Map<String, Map<String, Status>> endPointStatus) {
		this.endPointStatus = endPointStatus;
	}
	public void setEndPointStatus(String endPoint,Map<String, Status> endPointStatus) {
		this.endPointStatus.put(endPoint, endPointStatus);
	}
	public long getPeriod() {
		return period;
	}
	public void setPeriod(long period) {
		this.period = period;
	}
	public String getDestName() {
		return DestName;
	}
	public void setDestName(String destName) {
		DestName = destName;
	}
	public String getSrcName() {
		return SrcName;
	}
	public void setSrcName(String srcName) {
		SrcName = srcName;
	}
	public ScheduledFuture<?> getThread() {
		return thread;
	}
	public void setThread(ScheduledFuture<?> thread) {
		this.thread = thread;
	}
//	public Metering getMetering() {
//		return metering;
//	}
//	public void setMetering(Metering metering) {
//		this.metering = metering;
//	}

	public TimeMetering getTimeMetering() {
		return timeMetering;
	}

	public void setTimeMetering(TimeMetering timeMetering) {
		this.timeMetering = timeMetering;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void addTotalRows(int totalRows) {
		this.totalRows = this.totalRows+totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
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
