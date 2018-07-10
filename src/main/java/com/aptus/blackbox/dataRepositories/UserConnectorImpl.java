package com.aptus.blackbox.dataRepositories;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.UserConnectorDAO;
import com.aptus.blackbox.datamodels.ConnectorObj;
import com.aptus.blackbox.datamodels.UserConnector;
import com.aptus.blackbox.datamodels.UserInfo;

@Repository
public class UserConnectorImpl implements UserConnectorDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createUser(UserConnector _init) {
		mongoTemplate.save(_init);
		
	}

	@Override
	public List<ConnectorObj> getConnectorObjects(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		query.fields().include("srcdestId");
		System.out.println("sdfasdfsadfsadfsdfsdf");
		System.out.println(mongoTemplate.findOne(query, UserConnector.class));
		return null;
		
	}

	@Override
	public ConnectorObj getConnectorObject(String _id, String connectionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateConnectorObject(String _id, String connectionId, Map<String, Object> field) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteConnectorObject(String _id, String connectionId) {
		// TODO Auto-generated method stub
		return false;
	}

	

}
