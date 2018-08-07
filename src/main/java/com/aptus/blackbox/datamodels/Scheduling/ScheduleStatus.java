package com.aptus.blackbox.datamodels.Scheduling;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ScheduleStatus {

	@Id
	private String _id;
	private Map<String,Connection> connection=new HashMap<>();
	
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public Map<String,Connection> getConnection() {
		return connection;
	}
	public void setConnection(String connectionId,Connection connection) {
		this.connection.put(connectionId, connection);
	}
}
