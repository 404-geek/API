package com.aptus.blackbox.datamodels;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.UrlObject;

@Document
public class SourceConfig implements Serializable{
	@Id
	private String _id;
	private String name,authType,authLegs,status,steps,refresh;
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getAuthType() {
		return authType;
	}
	public void setAuthType(String authType) {
		this.authType = authType;
	}
	public String getAuthLegs() {
		return authLegs;
	}
	public void setAuthLegs(String authLegs) {
		this.authLegs = authLegs;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	private UrlObject refreshToken,requestToken,accessCode,accessToken,validateCredentials;
	private List<UrlObject> DataEndPoints,InfoEndpoints,ImplicitEndpoints;
	private List<List<List<String>>> infoEndpointOrder;
	
	public String getName() {
		return _id;
	}
	public void setName(String name) {
		this._id = name;
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
//	@Override
//	public String toString() {
//		return "_id:"+_id+" \nauthType:"+authType+"\nsteps:"+steps+"\nrefreshtoken:"+refreshToken+
//				"\nrequesttoken:"+requestToken+"\naccessCode:"+accessCode+"\naccessToken:"+accessToken
//				+"validateCredentiuals:"+validateCredentials+"dataendpoints\n"+DataEndPoints
//				+"\nInfoendpoints:"+InfoEndpoints+"\nImplicitendpoints:"+ImplicitEndpoints+
//				"\ninfoEndPointOrder:"+infoEndpointOrder;
//	}
}
