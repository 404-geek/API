package com.aptus.blackbox.datamodels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class SrcDestCredentials {

	@Id
	private String credentialId;
	private List<Map<String,String>> credentials;
	
	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}
	
	
	public SrcDestCredentials() {
		setCredentials(new ArrayList<Map<String,String>>());
	}
	
	public List<Map<String,String>> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<Map<String,String>> credentials) {
		this.credentials = credentials;
	}
	@Override
	public String toString() {
		return "CredentialsId:"+credentialId+"\n"
				+ "Credentials" + credentials;
	}
	
}

