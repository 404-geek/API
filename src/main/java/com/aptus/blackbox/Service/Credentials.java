package com.aptus.blackbox.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.index.ConnObj;
import com.aptus.blackbox.index.DestObject;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.index.SrcObject;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value="session")
public class Credentials implements Serializable {
	
	private String userId,currSrcName,currDestName;
	private ConnObj currConnId;
	private Map<String,ConnObj>connectionIds=new HashMap<>();
	private Map<String,String> sessionId=new HashMap<>();
	private boolean userExist,usrSrcExist,usrDestExist;
	private boolean currSrcValid,currDestValid;
	private Map<String,String> currSrcToken=new HashMap<>();
	private Map<String,String> currDestToken=new HashMap<>();
	private SrcObject currSrcObj;
	private DestObject currDestObj;
	private Map<String,SchedulingObjects> schedulingObjects = new HashMap<String,SchedulingObjects>();
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
	public Map<String,String> getCurrDestToken() {
		return currDestToken;
	}
	public void setCurrDestToken(Map<String,String> destToken) {
		this.currDestToken.putAll(destToken);
	}
	public void setCurrDestToken(String key,String value) {
		this.currDestToken.put(key, value);
	}
	public Map<String,String> getCurrSrcToken() {
		return currSrcToken;
	}
	public void setCurrSrcToken(Map<String,String> srcToken) {
		this.currSrcToken.putAll(srcToken);
	}
	public void setCurrSrcToken(String key,String value) {
		this.currSrcToken.put(key, value);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public SrcObject getCurrSrcObj() {
		return currSrcObj;
	}
	public void setCurrSrcObj(SrcObject srcObj) {
		this.currSrcObj = srcObj;
	}
	public DestObject getCurrDestObj() {
		return currDestObj;
	}
	public void setCurrDestObj(DestObject destObj) {
		this.currDestObj = destObj;
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
	public Map<String,SchedulingObjects> getSchedulingObjects() {
		return schedulingObjects;
	}
	public void setSchedulingObjects(Map<String,SchedulingObjects> schedulingObjects) {
		this.schedulingObjects.putAll(schedulingObjects);
	}
	public void setSchedulingObjects(SchedulingObjects schedulingObjects,String connectionId) {
		this.schedulingObjects.put(connectionId, schedulingObjects);
	}
	public void unSetSchedulingObjects(String connectionId) {
		this.schedulingObjects.remove(connectionId);
	}
}
