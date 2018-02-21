package com.aptus.blackbox.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.SrcObject;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value="session")
public class Credentials implements Serializable {
	
	private String userId,srcName,destName,connectionId;
	private Map<String,String> sessionId;
	private boolean userExist,usrSrcExist,usrDestExist;
	private Map<String,String> srcToken=new HashMap<>();
	private Map<String,String> destToken=new HashMap<>();
	private SrcObject srcObj;
	private DestObject destObj;
	
	public Map<String, String> getSessionId() {
		return sessionId;
	}
	public void setSessionId(String userId,String sessionId) {
		this.sessionId.clear();
		this.sessionId.put(userId, sessionId);
	}
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
		this.srcToken.put(key, value);
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
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
}
