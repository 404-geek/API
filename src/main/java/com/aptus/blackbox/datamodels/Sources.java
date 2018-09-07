package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Sources {
	@Id
	private String _id;
	private String name,logo;
	private List<String> refIndType,refBPDType,refAnaMthd,refDStype;
	private String SubscriptionType;
	

	public Sources() {
		refAnaMthd = new ArrayList<>();
		refIndType = new ArrayList<>();
		refDStype = new ArrayList<>();
		refBPDType = new ArrayList<>();
	}
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public List<String> getRefIndType() {
		return refIndType;
	}

	public void setRefIndType(List<String> refIndType) {
		this.refIndType = refIndType;
	}

	public List<String> getRefBPDType() {
		return refBPDType;
	}

	public void setRefBPDType(List<String> refBPDType) {
		this.refBPDType = refBPDType;
	}

	public List<String> getRefAnaMthd() {
		return refAnaMthd;
	}

	public void setRefAnaMthd(List<String> refAnaMthd) {
		this.refAnaMthd = refAnaMthd;
	}

	public List<String> getRefDStype() {
		return refDStype;
	}

	public void setRefDStype(List<String> refDStype) {
		this.refDStype = refDStype;
	}

	public String getSubscriptionType() {
		return SubscriptionType;
	}

	public void setSubscriptionType(String subscriptionType) {
		SubscriptionType = subscriptionType;
	}


	
}
