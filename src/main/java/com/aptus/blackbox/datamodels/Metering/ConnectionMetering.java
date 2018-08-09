package com.aptus.blackbox.datamodels.Metering;

import java.util.ArrayList;
import java.util.List;


public class ConnectionMetering {

	private List<TimeMetering> timeMetering = new ArrayList<>();
	private int totalRows;
	
	public int getTotalRows() {
		return totalRows;
	}
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}
	public List<TimeMetering> getTimeMetering() {
		return timeMetering;
	}	
	public void setTimeMetering(TimeMetering timeMetering) {
		this.timeMetering.add(timeMetering);
	}
	



}
