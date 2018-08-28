package com.aptus.blackbox.models;

import java.util.List;

public class srcdestObj{
	
	private String id,name,logo;
	private String refIndType ,refBPDType, refAnaMthd, refDStype;
	public String getRefIndType() {
		return refIndType;
	}
	public void setRefIndType(String refIndType) {
		this.refIndType = refIndType;
	}
	public String getRefBPDType() {
		return refBPDType;
	}
	public void setRefBPDType(String refBPDType) {
		this.refBPDType = refBPDType;
	}
	public String getRefAnaMthd() {
		return refAnaMthd;
	}
	public void setRefAnaMthd(String refAnaMthd) {
		this.refAnaMthd = refAnaMthd;
	}
	public String getRefDStype() {
		return refDStype;
	}
	public void setRefDStype(String refDStype) {
		this.refDStype = refDStype;
	}
	private List<String> categories;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

}