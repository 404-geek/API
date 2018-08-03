package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.MeteringData;
import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
import com.aptus.blackbox.datamodels.Metering.TimeMetering;

public interface MeteringDAO {

	public void createUser(MeteringData metering);
	public boolean updateMeteringData();
	boolean addConnection(String userId, String connectionId, ConnectionMetering connMeter);
	boolean addTimeMetering(String userId, String connectionId, TimeMetering timeMetering, long totalRows);
}
