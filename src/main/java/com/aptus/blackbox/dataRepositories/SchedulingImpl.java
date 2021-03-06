package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SchedulingDAO;
import com.aptus.blackbox.datamodels.Scheduling.Connection;
import com.aptus.blackbox.datamodels.Scheduling.ScheduleStatus;
import com.mongodb.WriteResult;

@Repository
public class SchedulingImpl implements SchedulingDAO{

	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void createUser(ScheduleStatus scheduleStatus) {
		mongoTemplate.save(scheduleStatus);
	}
	
	@Override
	public boolean addConnection(String userId,String connectionId,Connection connection) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(userId));
		Update update = new Update();
		WriteResult result = mongoTemplate.updateFirst(query,update.set("connection."+connectionId, connection) , ScheduleStatus.class);
		return result.wasAcknowledged();
	}


	@Override
	public boolean deleteConnection(String userId, String connectionId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(userId));
		Update update= new Update().unset("connection."+connectionId);
		WriteResult result = mongoTemplate.updateFirst(query, update, ScheduleStatus.class); 
		return result.wasAcknowledged();
	}

	@Override
	public long getScheduledConnCount(String userId) {
		ScheduleStatus data = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(userId)), ScheduleStatus.class);
		
		long a = data.getConnection().size();
		System.out.println("Schedule status MAp Size: "+a);
		return a;
	}

	@Override
	public ScheduleStatus getUserSchedulerData(String userId) {
		return mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(userId)), ScheduleStatus.class);		
	}
	
}
