package com.aptus.blackbox.DataService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.DomainObjects.ConnObj;
import com.aptus.blackbox.DomainObjects.DestObject;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.DomainObjects.SrcObject;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value="session")
public class Credentials implements Serializable {
	
	private String userId,currSrcName,currDestName;
	private ConnObj currConnId;
	private Map<String,ConnObj>connectionIds=new HashMap<>();
	private Map<String,String> sessionId=new HashMap<>();
	private boolean userExist,usrSrcExist,usrDestExist;
	private boolean currSrcValid,currDestValid;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private SrcObject SrcObj;
	private DestObject DestObj;
	
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
	public String getCurrSrcName() {
		return currSrcName;
	}
	public void setCurrSrcName(String srcName) {
		this.currSrcName = srcName;
	}
	public String getCurrDestName() {
		return currDestName;
	}
	public void setCurrDestName(String destName) {
		this.currDestName = destName;
	}
	public Map<String,String> getDestToken() {
		return DestToken;
	}
	public void setDestToken(Map<String,String> destToken) {
		this.DestToken.putAll(destToken);
	}
	public void setDestToken(String key,String value) {
		this.DestToken.put(key, value);
	}
	public Map<String,String> getSrcToken() {
		return SrcToken;
	}
	public void setSrcToken(Map<String,String> srcToken) {
		this.SrcToken.putAll(srcToken);
	}
	public void setSrcToken(String key,String value) {
		this.SrcToken.put(key, value);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public SrcObject getSrcObj() {
		return SrcObj;
	}
	public void setSrcObj(SrcObject srcObj) {
		this.SrcObj = srcObj;
	}
	public DestObject getDestObj() {
		return DestObj;
	}
	public void setDestObj(DestObject destObj) {
		this.DestObj = destObj;
	}
	public boolean isCurrSrcValid() {
		return currSrcValid;
	}
	public void setCurrSrcValid(boolean srcValid) {
		this.currSrcValid = srcValid;
	}
	public boolean isCurrDestValid() {
		return currDestValid;
	}
	public void setCurrDestValid(boolean destValid) {
		this.currDestValid = destValid;
	}
	public ConnObj getConnectionIds(String connId) {
		return connectionIds.get(connId);
	}
	public void setConnectionIds(String connectionId,ConnObj obj) {
		this.connectionIds.put(connectionId, obj);
	}
	public ConnObj getCurrConnId() {
		return currConnId;
	}
	public void setCurrConnId(ConnObj currConnId) {
		this.currConnId = currConnId;
	}
	
}
