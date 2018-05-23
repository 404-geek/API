package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.List;

public class UrlObject implements Serializable {
	
	private String label,type,url,method,responseString,responseBodyType,Authorization;
	private List<objects> params,header,responseBody,signature;
	private List<Cursor> pagination;
	private String data,catagory;	
	
	public List<objects> getSignature() {
		return signature;
	}
	public void setSignature(List<objects> signature) {
		this.signature = signature;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public List<objects> getParams() {
		return params;
	}
	public void setParams(List<objects> params) {
		this.params = params;
	}
	public List<objects> getHeader() {
		return header;
	}
	public void setHeader(List<objects> header) {
		this.header = header;
	}
	public List<objects> getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(List<objects> responseBody) {
		this.responseBody = responseBody;
	}
	public String getResponseString() {
		return responseString;
	}
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	public List<Cursor> getPagination() {
		return pagination;
	}
	public void setPagination(List<Cursor> pagination) {
		this.pagination = pagination;
	}
	public String getResponseBodyType() {
		return responseBodyType;
	}
	public void setResponseBodyType(String responseBodyType) {
		this.responseBodyType = responseBodyType;
	}
	public String getAuthorization() {
		return Authorization;
	}
	public void setAuthorization(String authorization) {
		Authorization = authorization;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getCatagory() {
		return catagory;
	}
	public void setCatagory(String catagory) {
		this.catagory = catagory;
	}
	
}
