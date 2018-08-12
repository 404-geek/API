package com.aptus.blackbox.dataInterfaces;

import java.util.List;
import java.util.Map;

import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.models.ConnObj;

public interface UserConnectorDAO {

	public void createUser(UserConnectors _init);
	public UserConnectors getUserConnector(String _id);
	
	public boolean updateConnectorObject(String _id,String connectionId,Map<String,Object> field);
    public boolean deleteConnectorObject(String _id,String connectionId);
    public boolean addConnectorObj(String _id,ConnObj connectorObj);
}
