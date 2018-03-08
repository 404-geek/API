package com.aptus.blackbox.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SchedulingObjects implements Serializable{
	private boolean SrcValid,DestValid;
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

}
