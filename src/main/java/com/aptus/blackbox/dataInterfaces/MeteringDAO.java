package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.MeteringData;
import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
import com.aptus.blackbox.datamodels.Metering.TimeMetering;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface MeteringDAO {

	public void createUser(MeteringData metering);
	public boolean updateMeteringData();
	boolean addConnection(String userId, String connectionId, ConnectionMetering connMeter);
	boolean addTimeMetering(String userId, String connectionId, TimeMetering timeMetering, int totalRows);
	public long getTotalRows(String userId);
}
