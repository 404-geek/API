package com.aptus.blackbox.dataRepositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.DestinationDAO;
import com.aptus.blackbox.datamodels.Destinations;

@Repository
public class DestinationImpl implements DestinationDAO {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Destinations> getDestinationList() {
		return mongoTemplate.findAll(Destinations.class);
	}

	@Override
	public void insert(Destinations destination) {
		mongoTemplate.save(destination);
	}

	@Override
	public boolean removeDestination(String _id) {
		// TODO Auto-generated method stub
		return false;
	}

}
