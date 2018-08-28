package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.MeteringDAO;
import com.aptus.blackbox.datamodels.MeteringData;
import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
import com.aptus.blackbox.datamodels.Metering.TimeMetering;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class MeteringService {

	@Autowired
	private  MeteringDAO meteringDAO;
	
	public void createUser(String userId) {
		MeteringData metering = new MeteringData();
		metering.setId(userId);
		meteringDAO.createUser(metering);
	}
	public boolean addConnection(String userId,String connectionId,ConnectionMetering connectionMetering) {
		return meteringDAO.addConnection(userId, connectionId, connectionMetering);
	}
	
	public boolean addTimeMetering(String userId, String connectionId, TimeMetering timeMetering, int totalRows) {
		
		return meteringDAO.addTimeMetering(userId, connectionId, timeMetering, totalRows);
	}
	
	public long totalRows(String userId) {
		return meteringDAO.getTotalRows(userId);
	}
	
}
