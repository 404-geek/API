package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.MeteringData;

public interface MeteringDAO {

	public void createUser(MeteringData metering);
	public boolean addConnection(String userId,String connectionId);
	public boolean updateMeteringData();
}
