package com.aptus.blackbox.dataRepositories;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SrcDestCredentialsDAO;

import com.aptus.blackbox.datamodels.SrcDestCredentials;
import com.mongodb.WriteResult;

@Repository
public class SrcDestCredentialsImpl implements SrcDestCredentialsDAO{

	@Autowired
	MongoTemplate mongoTemplate;
	
	@Override
	public void insertCredentials(SrcDestCredentials credential, String collection) {
		mongoTemplate.save(credential, collection);
	}

	@Override
	public boolean updateCredentials(String credentialId, List<Map<String,String>> credentials, String collection) {
		WriteResult res =  mongoTemplate.updateFirst(
				  new Query(Criteria.where("credentialId").is(credentialId)),
				  Update.update("credentials", credentials),SrcDestCredentials.class,collection);
		return res.wasAcknowledged();
	}

	@Override
	public SrcDestCredentials readCredentials(String credentialId, String collection) {
		Query query = new Query();
		query.addCriteria(Criteria.where("credentialId").is(credentialId));
		return mongoTemplate.findOne(query, SrcDestCredentials.class, collection);
		
	}
	}
