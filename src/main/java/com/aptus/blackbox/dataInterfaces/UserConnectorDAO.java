package com.aptus.blackbox.dataInterfaces;

import java.util.List;
import java.util.Map;

import com.aptus.blackbox.datamodels.ConnectorObj;
import com.aptus.blackbox.datamodels.UserConnector;

public interface UserConnectorDAO {

	public void createUser(UserConnector _init);
	public List<ConnectorObj> getConnectorObjects(String _id);
	public ConnectorObj getConnectorObject(String _id,String connectionId);
	public boolean updateConnectorObject(String _id,String connectionId,Map<String,Object> field);
    public boolean deleteConnectorObject(String _id,String connectionId);	
}
