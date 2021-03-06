package com.aptus.blackbox.dataService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.index.ScheduleInfo;

@Service
public class ApplicationCredentials implements Serializable {
	private Map<String,ScheduleInfo> applicationCred = new HashMap<String,ScheduleInfo>();
	private Map<String,String> sessionId=new HashMap<>();
	private Map<String,JSONObject> resourceUsage=new HashMap<>();
	
	
	public Map<String, String> getSessionId() {
		return sessionId;
	}
	public void setSessionId(String userId,String sessionId) {
		this.sessionId.put(userId, sessionId);
	}

	public String getSessionId(String userId) {
		return this.sessionId.get(userId);
	}
	public Map<String,ScheduleInfo> getApplicationCred() {
		return applicationCred;
	}
	public void setApplicationCred(Map<String,ScheduleInfo> applicationCred) {
		this.applicationCred = applicationCred;
	}
	public void setApplicationCred(String userId,ScheduleInfo scheduleInfo) {
		this.applicationCred.put(userId, scheduleInfo);
	}
	public Map<String,JSONObject> getResourceUsage(){
		return resourceUsage;
	}
	public void setResourceUsage(String time, JSONObject resourceUsage) {
		this.resourceUsage.put(time,resourceUsage);
	}
}
