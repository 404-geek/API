package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.UserConnectorDAO;
import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.models.ConnObj;
import com.google.gson.JsonObject;

@Service
public class UserConnectorService {

	@Autowired
	private UserConnectorDAO userConnectorDAO;
	
	public void createUser(String _id) {
		UserConnectors connector = new UserConnectors(_id);
		userConnectorDAO.createUser(connector);
	}
	
	public UserConnectors getConnectorObjects(String _id) {
		UserConnectors connector =  userConnectorDAO.getUserConnector(_id);
		return connector;
	}
	

	
	public boolean addConnectorObj(String _id,ConnObj connectorObj) {
		return userConnectorDAO.addConnectorObj(_id, connectorObj);
	}

	public boolean deleteConnectorObject(String _id,String connectionId) {
		return userConnectorDAO.deleteConnectorObject(_id, connectionId);
	}
	
	public  JsonObject countDataSourcesCreated(String _id){
		return userConnectorDAO.countDataSourcesCreated(_id);
	}
}
