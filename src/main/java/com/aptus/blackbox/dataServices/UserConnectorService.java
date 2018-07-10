package com.aptus.blackbox.dataServices;

import java.util.List;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.UserConnectorDAO;
import com.aptus.blackbox.datamodels.ConnectorObj;
import com.aptus.blackbox.datamodels.UserConnector;

@Service
public class UserConnectorService {

	@Autowired
	private UserConnectorDAO userConnectorDAO;
	
	public void createUser(String _id) {
		UserConnector connector = new UserConnector(_id);
		userConnectorDAO.createUser(connector);
	}
	
	public List<ConnectorObj> getConnectorObjects(String _id) {
		System.out.println("sdyiuhsdauisiu");
		return userConnectorDAO.getConnectorObjects(_id);
	}
	
	public ConnectorObj getConnectionObj(String _id,String connectionId) {
		return userConnectorDAO.getConnectorObject(_id, connectionId);
	}
	
	
}
