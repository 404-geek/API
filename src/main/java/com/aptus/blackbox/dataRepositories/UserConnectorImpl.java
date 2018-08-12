package com.aptus.blackbox.dataRepositories;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.UserConnectorDAO;
import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.models.ConnObj;

@Repository
public class UserConnectorImpl implements UserConnectorDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createUser(UserConnectors usrConn) {
		mongoTemplate.save(usrConn);
		
	}

	@Override
	public UserConnectors getUserConnector(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		query.fields().include("srcdestId");
		return mongoTemplate.findOne(query, UserConnectors.class);
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

	@Override
	public boolean addConnectorObj(String _id, ConnObj connectorObj) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		//mongoTemplate.updateFirst(query,new Update().addToSet("").each(values), UserConnector.class);
		mongoTemplate.updateFirst(query, new Update().addToSet("srcdestId", connectorObj), UserConnectors.class);
		return false;
	}

	

	

}
