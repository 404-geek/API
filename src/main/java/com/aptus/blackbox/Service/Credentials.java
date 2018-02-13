package com.aptus.blackbox.Service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.SrcObject;

@Service
public class Credentials {
	
	private String userId,srcName,destName;
	private boolean userExist,usrSrcExist,usrDestExist;
	private Map<String,String> srcToken=new HashMap<>();
	private Map<String,String> destToken=new HashMap<>();
	private SrcObject srcObj;
	private DestObject destObj;
	
	public boolean isUserExist() {
		return userExist;
	}
	public void setUserExist(boolean userExist) {
		this.userExist = userExist;
	}
	public boolean isUsrSrcExist() {
		return usrSrcExist;
	}
	public void setUsrSrcExist(boolean usrSrcExist) {
		this.usrSrcExist = usrSrcExist;
	}
	public boolean isUsrDestExist() {
		return usrDestExist;
	}
	public void setUsrDestExist(boolean usrDestExist) {
		this.usrDestExist = usrDestExist;
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
	public Map<String,String> getDestToken() {
		return destToken;
	}
	public void setDestToken(Map<String,String> destToken) {
		this.destToken.putAll(destToken);
	}
	public void setDestToken(String key,String value) {
		this.destToken.put(key, value);
	}
	public Map<String,String> getSrcToken() {
		return srcToken;
	}
	public void setSrcToken(Map<String,String> srcToken) {
		this.srcToken.putAll(srcToken);
	}
	public void setSrcToken(String key,String value) {
		this.destToken.put(key, value);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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
}
