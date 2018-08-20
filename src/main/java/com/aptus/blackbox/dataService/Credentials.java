package com.aptus.blackbox.dataService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;


import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.index.SchedulingObjects;
import com.aptus.blackbox.models.ConnObj;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value="session")
public class Credentials implements Serializable {
	
	private String userId,currSrcName,currDestName;
	private String currSrcId,currDestId;
	private ConnObj currConnObj;
	private Map<String,ConnObj>connectionIds=new HashMap<>();
	
	
	private boolean userExist,usrSrcExist,usrDestExist;
	private boolean currSrcValid,currDestValid;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private SourceConfig SrcObj;
	private DestinationConfig DestObj;
	
	
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
	public void addSrcToken(String key,String value) {
		this.SrcToken.put(key, value);
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public SourceConfig getSrcObj() {
		return SrcObj;
	}
	public void setSrcObj(SourceConfig srcObj) {
		this.SrcObj = srcObj;
	}
	public DestinationConfig getDestObj() {
		return DestObj;
	}
	public void setDestObj(DestinationConfig destObj) {
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
	public Map<String, ConnObj> getConnectionIds() {
		return this.connectionIds;
	}
	public void setConnectionIds(String connectionId,ConnObj obj) {
		this.connectionIds.put(connectionId, obj);
	}
	public ConnObj getCurrConnObj() {
		return currConnObj;
	}
	public void setCurrConnObj(ConnObj currConnObj) {
		this.currConnObj = currConnObj;
	}
	public String getCurrDestId() {
		return currDestId;
	}
	public void setCurrDestId(String currDestId) {
		this.currDestId = currDestId;
	}
	public String getCurrSrcId() {
		return currSrcId;
	}
	public void setCurrSrcId(String currSrcId) {
		this.currSrcId = currSrcId;
	}

	
}
