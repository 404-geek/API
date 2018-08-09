package com.aptus.blackbox.dataInterfaces;

import java.util.List;
import java.util.Map;


import com.aptus.blackbox.datamodels.SrcDestCredentials;

public interface SrcDestCredentialsDAO {
	public void insertCredentials(SrcDestCredentials credential,String collection);
	public boolean srcDestCredentialsExist(String credentialId,String collection);
	public SrcDestCredentials getCredentials(String credentialId,String collection);
	public SrcDestCredentials getCredentialsByRegex(String credentialIdRegex,String collection);
	public List<SrcDestCredentials> getAllCredentials(String credentialId,String collection);
	public List<SrcDestCredentials> getAllCredentialsByRegex(String credentialIdRegex,String collection);
	boolean updateCredentials(String credentialId, List<Map<String, String>> credentials, String collection);
}
