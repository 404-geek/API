package com.aptus.blackbox.dataRepositories;

import java.util.List;
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
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

@Repository
public class UserConnectorImpl implements UserConnectorDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	private static class LongValue {
        long  value;
    }
	
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
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		Update update = new Update().pull("srcdestId", new BasicDBObject("connectionId",connectionId));
		WriteResult res = mongoTemplate.updateFirst(query, update, UserConnectors.class);
		return res.wasAcknowledged();
	}

	@Override
	public boolean addConnectorObj(String _id, ConnObj connectorObj) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		//mongoTemplate.updateFt(query,new Update().addToSet("").each(values), UserConnector.class);
		mongoTemplate.updateFirst(query, new Update().addToSet("srcdestId", connectorObj), UserConnectors.class);
		return false;
	}

	@Override
	public JsonObject countDataSourcesCreated(String _id) {
		long downloads = 0;
		UserConnectors data = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(_id)),UserConnectors.class);
		List<ConnObj> arr = data.getConnectorObjs();
		for(int i=0;i<arr.size();i++) {
			if(arr.get(i).getDestName().equalsIgnoreCase("json") ||
					arr.get(i).getDestName().equalsIgnoreCase("csv") ||
					arr.get(i).getDestName().equalsIgnoreCase("xml"))
				downloads+=1;
		}
		JsonObject ob = new JsonObject();
		try {
			ob.addProperty("DatasourcesCreated", arr.size());
			ob.addProperty("FilesDownloaded", downloads);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return ob;		
	}

	

	

}
