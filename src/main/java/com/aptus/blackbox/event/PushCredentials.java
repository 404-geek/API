package com.aptus.blackbox.event;

import java.util.HashMap;
import java.util.Map;

import com.aptus.blackbox.models.DestObject;
import com.aptus.blackbox.models.SrcObject;

public class PushCredentials {
	private SrcObject srcObj;
	private DestObject destObj;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private String srcName,destName,userId;
	public PushCredentials(SrcObject srcObj, DestObject destObj,Map<String,String> SrcToken
			,Map<String,String> DestToken, String srcName,String destName,String userId) {
		this.srcName = srcName;
		this.srcObj = srcObj;
		this.destName=destName;
		this.destObj= destObj;
		this.userId=userId;
		this.DestToken = DestToken;
		this.SrcToken=SrcToken;
	}
	public SrcObject getSrcObj() {
		return srcObj;
	}
	public void setSrcObj(SrcObject srcObj) {
		this.srcObj = srcObj;
	}
	public DestObject getDestObj() {
		return destObj;
	}
	public void setDestObj(DestObject destObj) {
		this.destObj = destObj;
	}
	public String getSrcName() {
		return srcName;
	}
	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}
	public String getDestName() {
		return destName;
	}
	public void setDestName(String destName) {
		this.destName = destName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Map<String,String> getSrcToken() {
		return SrcToken;
	}
	public void setSrcToken(Map<String,String> srcToken) {
		SrcToken = srcToken;
	}
	public Map<String,String> getDestToken() {
		return DestToken;
	}
	public void setDestToken(Map<String,String> destToken) {
		DestToken = destToken;
	}
	
}
