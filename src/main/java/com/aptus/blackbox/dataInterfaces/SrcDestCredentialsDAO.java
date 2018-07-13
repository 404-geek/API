package com.aptus.blackbox.dataInterfaces;

import java.util.List;
import java.util.Map;


import com.aptus.blackbox.datamodels.SrcDestCredentials;

public interface SrcDestCredentialsDAO {
	public void insertCredentials(SrcDestCredentials credential,String collection);
	
	public SrcDestCredentials readCredentials(String credentialId,String collection);
	boolean updateCredentials(String credentialId, List<Map<String, String>> credentials, String collection);
}
