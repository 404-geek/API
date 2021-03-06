package com.aptus.blackbox.dataServices;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.SrcDestCredentialsDAO;
import com.aptus.blackbox.datamodels.SrcDestCredentials;

@Service
public class SrcDestCredentialsService {

	@Autowired
	private SrcDestCredentialsDAO srcDestCredentialDAO;
	
	public void insertCredentials(SrcDestCredentials credential,String collection) {
		srcDestCredentialDAO.insertCredentials(credential, collection);
	}
	
	public boolean updateCredentails(String credentialId,List<Map<String,String>> credentials,String collection){
		return srcDestCredentialDAO.updateCredentials(credentialId, credentials, collection);
	}
	
//	public SrcDestCredentials readCredentials(String credentialId,String collection) {
//		return srcDestCredentialDAO.getCredentials(credentialId, collection);
//	}

	public SrcDestCredentials getCredentials(String credentialId, String collection) {
		return srcDestCredentialDAO.getCredentials(credentialId, collection);
	}

	public boolean srcDestCredentialsExist(String credentialId, String collection) {
		return srcDestCredentialDAO.srcDestCredentialsExist(credentialId, collection);
	}

	public List<SrcDestCredentials> getAllCredentialsByRegex(String _id, String collectionDestinationcredentials) {
		
		return srcDestCredentialDAO.getAllCredentialsByRegex(_id, collectionDestinationcredentials);
	}

	
}
