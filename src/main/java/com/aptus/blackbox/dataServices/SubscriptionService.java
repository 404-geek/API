package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.SubscriptionDAO;
import com.aptus.blackbox.datamodels.Subscription;
import com.aptus.blackbox.models.SubscribedConnectors;
import com.aptus.blackbox.models.UserAppSubscription;
import com.mongodb.WriteResult;

@Service
public class SubscriptionService {

	@Autowired
	private SubscriptionDAO subscriptionDAO;
	
	public void createSubscription(String _id) {
		Subscription subscription = new Subscription();
		subscription.set_id(_id);
		subscriptionDAO.createSubscriber(subscription);
	}
	
	public boolean addsubscribedConnectors(String _id, SubscribedConnectors subscribedConnectors) {
		return subscriptionDAO.addsubscribedConnectors(_id, subscribedConnectors);
	}


	
	public boolean addAappSubscription(String _id, UserAppSubscription userAppSubscription) {
		return subscriptionDAO.addAappSubscription(_id, userAppSubscription);
	}
}
