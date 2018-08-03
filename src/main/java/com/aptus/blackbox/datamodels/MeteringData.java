package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
import com.aptus.blackbox.models.MeteredEndpoints;

@Document
public class MeteringData {


	@Id
	private String _id;
	private long totalRows;
	private Map<String,List<ConnectionMetering>> connection= new HashMap<>();
	
	public String getId() {
		return _id;
	}
	public void setId(String _id) {
		this._id = _id;
	}
	public long getTotalRows() {
		return totalRows;
	}
	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}
	public Map<String, List<ConnectionMetering>> getConnection() {
		return connection;
	}
	public void setConnection(String connId,ConnectionMetering connMetering) {
		
		if(connection.containsKey(connId))
			connection.get(connId).add(connMetering);
		else {
			connection.put(connId, new ArrayList<>());
			connection.get(connId).add(connMetering);
		}
	}

	
}
