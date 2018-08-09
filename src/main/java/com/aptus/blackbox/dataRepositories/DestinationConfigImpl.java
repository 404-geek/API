package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.DestinationConfigDAO;
import com.aptus.blackbox.datamodels.DestinationConfig;

@Repository
public class DestinationConfigImpl implements DestinationConfigDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createDestinationConfig(DestinationConfig destinationConfig) {
		mongoTemplate.save(destinationConfig);		
	}

	@Override
	public DestinationConfig getDestinationConfig(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		return mongoTemplate.findOne(query, DestinationConfig.class);
		
	}

	@Override
	public boolean updateDestinationConfig(String _id, DestinationConfig destinationConfig) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteDestinationConfig(String _id) {
		// TODO Auto-generated method stub
		return false;
	}

}
