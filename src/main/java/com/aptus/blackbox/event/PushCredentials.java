package com.aptus.blackbox.event;

import java.util.HashMap;
import java.util.Map;

import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;

public class PushCredentials {
	private SourceConfig srcObj;
	private DestinationConfig destObj;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private String srcName,destName,userId;
	public PushCredentials(SourceConfig srcObj, DestinationConfig destObj,Map<String,String> SrcToken
			,Map<String,String> DestToken, String srcName,String destName,String userId) {
		this.srcName = srcName;
		this.srcObj = srcObj;
		this.destName=destName;
		this.destObj= destObj;
		this.userId=userId;
		this.DestToken = DestToken;
		this.SrcToken=SrcToken;
	}
	public SourceConfig getSrcObj() {
		return srcObj;
	}
	public void setSrcObj(SourceConfig srcObj) {
		this.srcObj = srcObj;
	}
	public DestinationConfig getDestObj() {
		return destObj;
	}
	public void setDestObj(DestinationConfig destObj) {
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
