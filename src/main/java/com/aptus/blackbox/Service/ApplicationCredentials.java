package com.aptus.blackbox.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aptus.blackbox.index.ScheduleInfo;

@Service
public class ApplicationCredentials implements Serializable {
	private Map<String,ScheduleInfo> applicationCred = new HashMap<String,ScheduleInfo>();

	public Map<String,ScheduleInfo> getApplicationCred() {
		return applicationCred;
	}
	public void setApplicationCred(Map<String,ScheduleInfo> applicationCred) {
		this.applicationCred = applicationCred;
	}
	public void setApplicationCred(String userId,ScheduleInfo applicationCred) {
		this.applicationCred.put(userId, applicationCred);
	}
}
