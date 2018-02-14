package com.aptus.blackbox.index;

import java.util.List;

public class UrlObject {
	
	private String label,type,url,method,responseString;
	private List<objects> params,header,responseBody,signature;
	
	
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
	
}
