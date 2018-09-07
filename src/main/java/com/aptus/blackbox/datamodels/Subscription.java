package com.aptus.blackbox.datamodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.SubscribedConnectors;
import com.aptus.blackbox.models.UserAppSubscription;

@Document
public class Subscription implements Serializable {
	
	@Id
	private String _id;	
	private List<SubscribedConnectors> subscribedConnectors;
	private List<UserAppSubscription> appSubscription;
	
	public Subscription() {
		setAppSubscriptionObjs(new ArrayList<UserAppSubscription>());
		setSubscribedConnectorObjs(new ArrayList<SubscribedConnectors>());
	}	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public List<SubscribedConnectors> getSubscribedConnectorObjs() {
		return subscribedConnectors;
	}
	public void setSubscribedConnectorObjs(List<SubscribedConnectors> subscribedConnectorObjs) {
		this.subscribedConnectors = subscribedConnectorObjs;
	}
	public List<UserAppSubscription> getAppSubscriptionObjs() {
		return appSubscription;
	}
	public void setAppSubscriptionObjs(List<UserAppSubscription> subscriptionObjs) {
		this.appSubscription = subscriptionObjs;
	}
	
}
