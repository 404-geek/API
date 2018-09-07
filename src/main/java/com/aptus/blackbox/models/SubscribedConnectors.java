package com.aptus.blackbox.models;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

public class SubscribedConnectors implements Serializable{
	
	@Id
	private String connectorSubId;
	private String connectorSubPurchaseDate,connectorSubStartDate,connectorSubEndDate;
	public String getConnectorSubId() {
		return connectorSubId;
	}
	public void setConnectorSubId(String connectorSubId) {
		this.connectorSubId = connectorSubId;
	}
	public String getConnectorSubPurchaseDate() {
		return connectorSubPurchaseDate;
	}
	public void setConnectorSubPurchaseDate(String connectorSubPurchaseDate) {
		this.connectorSubPurchaseDate = connectorSubPurchaseDate;
	}
	public String getConnectorSubStartDate() {
		return connectorSubStartDate;
	}
	public void setConnectorSubStartDate(String connectorSubStartDate) {
		this.connectorSubStartDate = connectorSubStartDate;
	}
	public String getConnectorSubEndDate() {
		return connectorSubEndDate;
	}
	public void setConnectorSubEndDate(String connectorSubEndDate) {
		this.connectorSubEndDate = connectorSubEndDate;
	}

	
}
