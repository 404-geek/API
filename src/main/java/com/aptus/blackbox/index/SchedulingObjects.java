package com.aptus.blackbox.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SchedulingObjects implements Serializable{
	private boolean SrcValid,DestValid;
	private String status,message,threadName,sourceThread,destThread;
	private Map<String,String> SrcToken=new HashMap<>();
	private Map<String,String> DestToken=new HashMap<>();
	private SrcObject SrcObj;
	private DestObject DestObj;
	public boolean isSrcValid() {
		return SrcValid;
	}
	public void setSrcValid(boolean srcValid) {
		SrcValid = srcValid;
	}
	public boolean isDestValid() {
		return DestValid;
	}
	public void setDestValid(boolean destValid) {
		DestValid = destValid;
	}
	public Map<String, String> getSrcToken() {
		return SrcToken;
	}
	public void setSrcToken(Map<String, String> srcToken) {
		SrcToken = srcToken;
	}
	public Map<String, String> getDestToken() {
		return DestToken;
	}
	public void setDestToken(Map<String, String> destToken) {
		DestToken = destToken;
	}
	public SrcObject getSrcObj() {
		return SrcObj;
	}
	public void setSrcObj(SrcObject srcObj) {
		SrcObj = srcObj;
	}
	public DestObject getDestObj() {
		return DestObj;
	}
	public void setDestObj(DestObject destObj) {
		DestObj = destObj;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getThreadName() {
		return threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public String getSourceThread() {
		return sourceThread;
	}
	public void setSourceThread(String sourceThread) {
		this.sourceThread = sourceThread;
	}
	public String getDestThread() {
		return destThread;
	}
	public void setDestThread(String destThread) {
		this.destThread = destThread;
	}

}
