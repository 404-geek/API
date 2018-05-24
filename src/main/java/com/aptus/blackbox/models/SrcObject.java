package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.List;

public class SrcObject implements Serializable {
	private String name,authType,steps,refresh;
	private UrlObject refreshToken,requestToken,accessCode,accessToken,validateCredentials;
	private List<UrlObject> DataEndPoints,InfoEndpoints,ImplicitEndpoints;
	private List<List<List<String>>> infoEndpointOrder;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthtype() {
		return authType;
	}
	public void setAuthtype(String authtype) {
		this.authType = authtype;
	}
	public String getSteps() {
		return steps;
	}
	public void setSteps(String steps) {
		this.steps = steps;
	}
	public UrlObject getAccessCode() {
		return accessCode;
	}
	public void setAccessCode(UrlObject accessCode) {
		this.accessCode = accessCode;
	}
	public UrlObject getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(UrlObject accessToken) {
		this.accessToken = accessToken;
	}
	public UrlObject getValidateCredentials() {
		return validateCredentials;
	}
	public void setValidateCredentials(UrlObject validateCredentials) {
		this.validateCredentials = validateCredentials;
	}
	public UrlObject getRequestToken() {
		return requestToken;
	}
	public void setRequestToken(UrlObject requestToken) {
		this.requestToken = requestToken;
	}
	public UrlObject getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(UrlObject refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getRefresh() {
		return refresh;
	}
	public void setRefresh(String refresh) {
		this.refresh = refresh;
	}
	public List<UrlObject> getDataEndPoints() {
		return DataEndPoints;
	}
	public void setDataEndPoints(List<UrlObject> dataEndPoints) {
		DataEndPoints = dataEndPoints;
	}
	public List<UrlObject> getInfoEndpoints() {
		return InfoEndpoints;
	}
	public void setInfoEndpoints(List<UrlObject> infoEndpoints) {
		InfoEndpoints = infoEndpoints;
	}
	public List<UrlObject> getImplicitEndpoints() {
		return ImplicitEndpoints;
	}
	public void setImplicitEndpoints(List<UrlObject> implicitEndpoints) {
		ImplicitEndpoints = implicitEndpoints;
	}
	public List<List<List<String>>> getInfoEndpointOrder() {
		return infoEndpointOrder;
	}
	public void setInfoEndpointOrder(List<List<List<String>>> infoEndpointOrder) {
		this.infoEndpointOrder = infoEndpointOrder;
	}
	
}
