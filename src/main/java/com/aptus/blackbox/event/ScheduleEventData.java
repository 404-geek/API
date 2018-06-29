package com.aptus.blackbox.event;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ScheduleEventData {

	private String userId,connId;
	private long period;	
	private boolean first;

	public String getUserId() {
		return userId;
	}

	public void setData(String userId,String connId,long period,boolean first) {
		this.userId = userId;
		this.connId=connId;
		this.period=period;
		this.first = first;
	}

	public String getConnId() {
		return connId;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

}
