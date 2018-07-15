package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SourceConfigDAO;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.datamodels.UserInfo;

@Repository
public class SourceConfigImpl implements SourceConfigDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createSourceConfig(SourceConfig sourceConfig) {
		mongoTemplate.save(sourceConfig);		
	}

	@Override
	public SourceConfig getSourceConfig(String _id) {
		Query query = new Query();
		
		query.addCriteria(Criteria.where("_id").is(_id));
		return mongoTemplate.findOne(query, SourceConfig.class);	
	}

	@Override
	public boolean updateSourceConfig(String source, SourceConfig sourcConfig) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteSourceConfig(String source) {
		// TODO Auto-generated method stub
		return false;
	}

}
