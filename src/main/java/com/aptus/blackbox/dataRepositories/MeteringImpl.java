package com.aptus.blackbox.dataRepositories;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.MeteringDAO;
import com.aptus.blackbox.datamodels.MeteringData;
import com.mongodb.WriteResult;

@Repository
public class MeteringImpl implements MeteringDAO{

	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createUser(MeteringData metering) {
		mongoTemplate.save(metering);		
	}

	@Override
	public boolean updateMeteringData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addConnection(String userId, String connectionId) {
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(userId));
		WriteResult result = mongoTemplate.updateFirst(query,
			new	Update()..update(connectionId, new ArrayList<>()),MeteringData.class);
		return false;
	}

	
}
