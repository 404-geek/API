package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;

public interface DestinationConfigDAO {

	public void createDestinationConfig(DestinationConfig destinationConfig);
	public DestinationConfig getDestinationConfig(String _id);
	public boolean updateDestinationConfig(String _id,DestinationConfig destinationConfig);
	public boolean deleteDestinationConfig(String _id);

}
