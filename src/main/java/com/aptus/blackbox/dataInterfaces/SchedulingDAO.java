package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.Scheduling.Connection;
import com.aptus.blackbox.datamodels.Scheduling.ScheduleStatus;

public interface SchedulingDAO {
	public void createUser(ScheduleStatus scheduleStatus);

	public boolean addConnection(String userId, String connectionId, Connection connection);
}
