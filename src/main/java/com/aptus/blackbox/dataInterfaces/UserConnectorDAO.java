package com.aptus.blackbox.dataInterfaces;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.models.ConnObj;
import com.google.gson.JsonObject;

public interface UserConnectorDAO {

	public void createUser(UserConnectors _init);
	public UserConnectors getUserConnector(String _id);
	
	public boolean updateConnectorObject(String _id,String connectionId,Map<String,Object> field);
    public boolean deleteConnectorObject(String _id,String connectionId);
    public boolean addConnectorObj(String _id,ConnObj connectorObj);
    public  JsonObject  countDataSourcesCreated(String _id); 
}
