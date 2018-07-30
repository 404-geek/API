package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.MeteredEndpoints;

@Document
public class MeteringData {


	@Id
	private String _id;
	private long usrTotalRows;
	private Map<String,List<ConnectionMetering>> connection= new HashMap<>();
	
	public String getId() {
		return _id;
	}
	public void setId(String _id) {
		this._id = _id;
	}
	public long getUsrTotalRows() {
		return usrTotalRows;
	}
	public void setUsrTotalRows(long usrTotalRows) {
		this.usrTotalRows = usrTotalRows;
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

	
	private class ConnectionMetering{
		private List<TimeMetering> timeMetering = new ArrayList<>();
		private long totalRows;
		
		public long getTotalRows() {
			return totalRows;
		}
		public void setTotalRows(long totalRows) {
			this.totalRows = totalRows;
		}
		public List<TimeMetering> getTimeMetering() {
			return timeMetering;
		}	
		public void setTimeMetering(TimeMetering timeMetering) {
			this.timeMetering.add(timeMetering);
		}
		


		private class TimeMetering{
			private long totalRows;
			private String type,time;
			private Map<String,List<EndpointMetering>> endpoints= new HashMap<>();
			
			public long getTotalRows() {
				return totalRows;
			}
			public void setTotalRows(long totalRows) {
				this.totalRows = totalRows;
			}
			public String getType() {
				return type;
			}
			public void setType(String type) {
				this.type = type;
			}
			public String getTime() {
				return time;
			}
			public void setTime(String time) {
				this.time = time;
			}
			public Map<String, List<EndpointMetering>> getEndpoints() {
				return endpoints;
			}
			public void setEndpoints(String category,EndpointMetering endpoint) {
				if(this.endpoints.containsKey(category))
					this.endpoints.get(category).add(endpoint);
				else {
					this.endpoints.put(category, new ArrayList<>());
					this.endpoints.get(category).add(endpoint);
				}
			}

			

			private class EndpointMetering {
				private String endpoint;
				private long totalRows;
				
				public String getEndpoint() {
					return endpoint;
				}
				public void setEndpoint(String endpoint) {
					this.endpoint = endpoint;
				}
				public long getTotalRows() {
					return totalRows;
				}
				public void setTotalRows(long totalRows) {
					this.totalRows = totalRows;
				}
				
				
				
			}
		}
	}
}
