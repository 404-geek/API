package com.aptus.blackbox.dataServices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataService.Credentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;





@Service
public class WebSocketService {
	
	@Autowired
	private Credentials credentials;
	@Autowired
	private UserConnectorService userConnectorService;
	@Autowired
	private MeteringService meteringService;
	@Autowired
	private SchedulingService schedulingService;

	private SimpMessagingTemplate template;
	final Logger logger = LogManager.getLogger(WebSocketService.class.getPackage());

	@Autowired
	public WebSocketService(SimpMessagingTemplate template) {
		System.out.println("WebSocketService SimpleMessaging Constructor called");
		
		/* Initialize messaging template */
		this.template = template;
	}
	

	public void sendUserStatistics() {
		
		JsonObject statistics= new JsonObject();
		JsonObject obj = new JsonObject();
		String userId = credentials.getUserId();
		
		obj=userConnectorService.countDataSourcesCreated(userId);//fetches count of files and ds created
		obj.addProperty("RowsFetced", meteringService.getTotalRows(userId) );
		obj.addProperty("DatasourcesScheduled", schedulingService.scheduleConnectionCount(userId));
		
		statistics.addProperty("code","200");
		statistics.addProperty("message", "Statistics data updated");
		statistics.add("data", obj);
		System.out.println("Socket data sended: "+statistics);
		
		
		template.convertAndSend("/chat", statistics.toString());
	}
	

	
	

}
