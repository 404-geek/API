package com.aptus.blackbox.dataRepositories;



import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.UserInfoDAO;
import com.aptus.blackbox.datamodels.UserInfo;
import com.mongodb.WriteResult;

@Repository
public class UserInfoImpl implements UserInfoDAO{

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public UserInfo getUserById(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		return mongoTemplate.findOne(query, UserInfo.class);
	}
	
	@Override
	public UserInfo getUserByEmail(String email) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(email));
		return mongoTemplate.findOne(query, UserInfo.class);
	}
	
	@Override
	public List<UserInfo> getAllUsers() {
		return mongoTemplate.findAll(UserInfo.class);
	}

	@Override
	public UserInfo createUser(UserInfo userInfo) {
		 mongoTemplate.save(userInfo);
		 return userInfo;
	}

	@Override
	public boolean deleteUser(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(_id));
		WriteResult result=mongoTemplate.remove(query, UserInfo.class);
		return result.wasAcknowledged();
	}
	
	@Override
	public boolean matchSingleField(String _id,String key, String value ) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id).and(key).is(value));
		return mongoTemplate.count(query, UserInfo.class)==1;
	}
	
	@Override
	public boolean matchMultipleField(String _id,Map<String,String> fields ) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		for(Map.Entry<String, String>  field: fields.entrySet()) {
			query.addCriteria(Criteria.where(field.getKey()).is(field.getValue()));
		}
		return mongoTemplate.count(query, UserInfo.class)==1;
	}

	@Override
	public UserInfo updateUser(String _id, String key, Object value) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		WriteResult result = mongoTemplate.updateFirst(query, Update.update(key, value), UserInfo.class);
		if(result.wasAcknowledged())
			return mongoTemplate.findOne(query, UserInfo.class);
		else
			return null;
	}

	
}
