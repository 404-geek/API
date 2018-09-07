package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.ConnObj;

@Document
public class UserConnectors {

	@Id
	private String _id;
	private List<ConnObj> srcdestId;
	
	public UserConnectors(String _id) {
		this._id = _id;
		setConnectorObjs(new ArrayList<ConnObj>());
	}
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public List<ConnObj> getConnectorObjs() {
		return srcdestId;
	}
	public void setConnectorObjs(List<ConnObj> connectorObjs) {
		this.srcdestId = connectorObjs;
	}
	
	
		
}
