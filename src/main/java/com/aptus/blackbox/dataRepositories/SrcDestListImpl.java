package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SrcDestListDAO;
import com.aptus.blackbox.datamodels.SrcDestList;

@Repository
public class SrcDestListImpl  implements SrcDestListDAO{

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public SrcDestList getSrcDestList(String _id) {
		
		Query query =new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		SrcDestList res = mongoTemplate.findOne(query, SrcDestList.class,"SrcDstlist");
		
		return res;
	}

}
