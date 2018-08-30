package com.aptus.blackbox.dataRepositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SourceDAO;
import com.aptus.blackbox.datamodels.Sources;

@Repository
public class SourceImpl implements SourceDAO{

	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Sources> getSourceList() {
		return mongoTemplate.findAll(Sources.class);
	}

	@Override
	public void insert(Sources source) {
		mongoTemplate.save(source);
	}

	@Override
	public boolean removeSource(String _id) {
		// TODO Auto-generated method stub
		return false;
	}

}
