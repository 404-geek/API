
package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class DestinationCredentials extends PairObj {

	@Id
	private String _id;
	private List<PairObj> credentials;
	
	public DestinationCredentials() {
		setCredentials(new ArrayList<PairObj>());
	}
	
	public List<PairObj> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<PairObj> credentials) {
		this.credentials = credentials;
	}
	
}


