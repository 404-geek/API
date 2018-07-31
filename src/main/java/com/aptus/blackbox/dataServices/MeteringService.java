package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.MeteringDAO;
import com.aptus.blackbox.datamodels.MeteringData;

@Service
public class MeteringService {

	@Autowired
	private  MeteringDAO meteringDAO;
	
	public void createUser(String userId) {
		MeteringData metering = new MeteringData();
		metering.setId(userId);
		meteringDAO.createUser(metering);
	}
	public boolean addConnection(String userId,String connectionId) {
		return meteringDAO.addConnection(userId, connectionId);
	}
	
	
	
}
