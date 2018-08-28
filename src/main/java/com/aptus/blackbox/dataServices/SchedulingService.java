package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.SchedulingDAO;
import com.aptus.blackbox.datamodels.Scheduling.Connection;
import com.aptus.blackbox.datamodels.Scheduling.ScheduleStatus;

@Service
public class SchedulingService {

	
	@Autowired
	private SchedulingDAO schedulingDAO;
	
	
	public void createUser(String userId) {
		ScheduleStatus schedulingStatus = new ScheduleStatus();
		schedulingStatus.set_id(userId);
		schedulingDAO.createUser(schedulingStatus);
	}
	
	public boolean addConnection(String userId,String connectionId, Connection connection) {
		
		return schedulingDAO.addConnection(userId, connectionId, connection);
	}

	public boolean deleteConnection(String userId, String connectionId) {
		return schedulingDAO.deleteConnection(userId, connectionId);
	}
	
	public long scheduleConnectionCount(String userId) {
		return schedulingDAO.getScheduledConnCount(userId);
	}
}
