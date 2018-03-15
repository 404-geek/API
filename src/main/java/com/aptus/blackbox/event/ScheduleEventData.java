package com.aptus.blackbox.event;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ScheduleEventData {

	private String userId,connId,scheduled;
	private String period;
	

	public String getUserId() {
		return userId;
	}

	public void setData(String userId,String connId,String scheduled,String period) {
		this.userId = userId;
		this.connId=connId;
		this.scheduled=scheduled;
		this.period=period;
	}

	public String getConnId() {
		return connId;
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



}
