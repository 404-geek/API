package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.DestinationConfigDAO;
import com.aptus.blackbox.datamodels.DestinationConfig;

@Service
public class DestinationConfigService {

	@Autowired
	private DestinationConfigDAO destinationConfigDAO;
	
	public void createDestinationConfig(DestinationConfig destinationConfig)
	{
		destinationConfigDAO.createDestinationConfig(destinationConfig);
		
	}
	public DestinationConfig getDestinationConfig(String _id) {
		
		return destinationConfigDAO.getDestinationConfig(_id);
	}
	public boolean updateDestinationConfig(String _id,DestinationConfig destinationConfig) {
		
		return destinationConfigDAO.updateDestinationConfig(_id, destinationConfig);
	}
	public boolean deleteDestinationConfig(String _id) {
		return destinationConfigDAO.deleteDestinationConfig(_id);
	}
	
}
