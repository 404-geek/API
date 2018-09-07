package com.aptus.blackbox.event;

import java.util.Locale;

import com.aptus.blackbox.datamodels.UserInfo;

public class OnRegistrationCompleteEvent {
	
	private String appUrl;
    private Locale locale;
    private UserInfo userInfo;
 
    public OnRegistrationCompleteEvent( UserInfo userInfo, Locale locale, String appUrl) {
        
         
        this.userInfo = userInfo;
        this.locale = locale;
        this.appUrl = appUrl;
    }

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	

}
