package com.aptus.blackbox.dataInterfaces;

import java.util.List;

import com.aptus.blackbox.datamodels.Destinations;

public interface DestinationDAO {
	 public List<Destinations> getDestinationList();
	 public void insert(Destinations destination);
	 public boolean removeDestination(String _id);
}
