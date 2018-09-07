package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.VerificationTokenDAO;
import com.aptus.blackbox.datamodels.VerificationToken;
import com.mongodb.WriteResult;

@Repository
public class VerificationTokenImpl implements VerificationTokenDAO{

	
	@Autowired
	private MongoTemplate mongoTemplate;
	

	
	public VerificationTokenImpl() {
		// TODO Auto-generated constructor stub
	}



	@Override
	public void createVerificationToken(String userId, String token) {
		 VerificationToken myToken = new VerificationToken(token, userId);
		 mongoTemplate.save(myToken);
		 
	}



	@Override
	public VerificationToken getVerificationToken(String token) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("token").is(token));
		return mongoTemplate.findOne(query, VerificationToken.class);
		
	}



	@Override
	public boolean invalidateToken(String token) {

		Query query = new Query();
		query.addCriteria(Criteria.where("token").is(token));
		Update update = new Update();
		update.set("isValid", false);
		WriteResult result = mongoTemplate.updateFirst(query, update, VerificationToken.class);
		return result.wasAcknowledged();
	}
	
	

}
