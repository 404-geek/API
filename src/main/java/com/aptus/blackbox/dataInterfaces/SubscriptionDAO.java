package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.Subscription;
import com.aptus.blackbox.models.SubscribedConnectors;
import com.aptus.blackbox.models.UserAppSubscription;

public interface SubscriptionDAO {
	
	public void    createSubscriber(Subscription subscription);
	public boolean addsubscribedConnectors(String _id,SubscribedConnectors subscribedConnectors);
	public boolean addAappSubscription(String _id,UserAppSubscription userAppSubscription);
	public boolean updateUserConnectorSubscription();
	public boolean deleteUserConnectorSubscription();
	public boolean updateUserAppSubscription();
	public boolean deleteUserAppSubscription();
	public boolean moveHistoricData();
	public boolean updateUserConnectorSubscription(String _id, String connectorid, String key, String value);
}
