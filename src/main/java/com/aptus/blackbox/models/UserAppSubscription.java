package com.aptus.blackbox.models;

import org.springframework.data.annotation.Id;

public class UserAppSubscription {
	
	@Id
	private String appSubId;
	private String appSubPurchaseDate,appSubStartDate,appSubEndDate;
	public String getAppSubId() {
		return appSubId;
	}
	public void setAppSubId(String appSubId) {
		this.appSubId = appSubId;
	}
	public String getAppSubPurchaseDate() {
		return appSubPurchaseDate;
	}
	public void setAppSubPurchaseDate(String appSubPurchaseDate) {
		this.appSubPurchaseDate = appSubPurchaseDate;
	}
	public String getAppSubStartDate() {
		return appSubStartDate;
	}
	public void setAppSubStartDate(String appSubStartDate) {
		this.appSubStartDate = appSubStartDate;
	}
	public String getAppSubEndDate() {
		return appSubEndDate;
	}
	public void setAppSubEndDate(String appSubEndDate) {
		this.appSubEndDate = appSubEndDate;
	}
	
}
