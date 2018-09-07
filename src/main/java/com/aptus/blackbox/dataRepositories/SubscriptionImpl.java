package com.aptus.blackbox.dataRepositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.SubscriptionDAO;
import com.aptus.blackbox.datamodels.Subscription;
import com.aptus.blackbox.models.SubscribedConnectors;
import com.aptus.blackbox.models.UserAppSubscription;
import com.mongodb.WriteResult;

@Repository
public class SubscriptionImpl implements SubscriptionDAO {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void createSubscriber(Subscription subscription) {
		mongoTemplate.save(subscription);		
	}


	@Override
	public boolean updateUserConnectorSubscription() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUserConnectorSubscription() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateUserAppSubscription() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteUserAppSubscription() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean moveHistoricData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateUserConnectorSubscription(String _id, String connectorid, String key, String value) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean addsubscribedConnectors(String _id, SubscribedConnectors subscribedConnectors) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		Update update =new Update();
		update.addToSet("subscribedConnectors",subscribedConnectors);
		WriteResult res =mongoTemplate.updateFirst(query, update, Subscription.class);
		return res.wasAcknowledged();
	}


	@Override
	public boolean addAappSubscription(String _id, UserAppSubscription userAppSubscription) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		Update update =new Update();
		update.addToSet("appSubscription",userAppSubscription);
		WriteResult res =mongoTemplate.updateFirst(query, update, Subscription.class);
		return res.wasAcknowledged();
	}
	



	
}
