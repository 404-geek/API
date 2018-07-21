package com.aptus.blackbox.dataServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.UserConnectorDAO;
import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.models.ConnObj;

@Service
public class UserConnectorService {

	@Autowired
	private UserConnectorDAO userConnectorDAO;
	
	public void createUser(String _id) {
		UserConnectors connector = new UserConnectors(_id);
		userConnectorDAO.createUser(connector);
	}
	
	public List<ConnObj> getConnectorObjects(String _id) {
		System.out.println("sdyiuhsdauisiu");
		return userConnectorDAO.getConnectorObjects(_id);
	}
	
	public ConnObj getConnectionObj(String _id,String connectionId) {
		return userConnectorDAO.getConnectorObject(_id, connectionId);
	}
	
	public boolean addConnectorObj(String _id,ConnObj connectorObj) {
		return userConnectorDAO.addConnectorObj(_id, connectorObj);
	}
	
}
