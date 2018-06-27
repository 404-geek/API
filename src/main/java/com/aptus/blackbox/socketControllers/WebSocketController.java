package com.aptus.blackbox.socketControllers;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
public class WebSocketController {
	
	@Autowired
	private Config config;
	
	@Autowired
	private Credentials credentials;

	private SimpMessagingTemplate template;
	@Autowired
	public WebSocketController(SimpMessagingTemplate template) {
		this.template = template;
	}
	
//    @RequestMapping("/socket")
//    public void greet(@RequestParam(value="user") String user) {
//    	
//    	HttpHeaders headers = new HttpHeaders();
//		headers.add("Cache-Control", "no-cache");
//		headers.add("access-control-allow-origin", config.getRootUrl());
//        headers.add("access-control-allow-credentials", "true");
//    	
//    	Gson gson = new Gson();
//    	
//    	RestTemplate restTemplate = new RestTemplate();
//		HttpHeaders header = new HttpHeaders();
//		HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
//		
//		String url = config.getMongoUrl()+"/credentials/userCredentials?filter={\"_id\":\""+user.toLowerCase()+"\"}";
//		URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
//
//		JsonObject UserCred  = gson.fromJson((restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody()),JsonObject.class);
//		
//		url = config.getMongoUrl()+"/credentials/metering/"+user.toLowerCase();
//		uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
//
//		JsonObject MeteringCred  = gson.fromJson((restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class).getBody()),JsonObject.class);
//		
//		int numDataSources=0,
//		scheduledDataSources=0,
//		numDownloaded=0,
//		numRows=0;
//		
//		if(UserCred.get("_returned").getAsInt()==0) {
//			numDataSources=0;
//			scheduledDataSources=0;
//			numDownloaded=0;
//			numRows=0;
//		}
//		else {
//			JsonObject embed = UserCred.get("_embedded").getAsJsonArray().get(0).getAsJsonObject();
//			for(JsonElement srcdest:embed.get("srcdestId").getAsJsonArray()) {
//				if(srcdest.isJsonObject()) {
//					JsonObject temp = srcdest.getAsJsonObject();
//					numDataSources++;
//					if(temp.get("scheduled").getAsString().equalsIgnoreCase("true")) {
//						scheduledDataSources++;
//					}					
//				}
//			}
//			numRows = MeteringCred.get("Total rows").getAsInt();
//			for(Entry<String, JsonElement> conn:MeteringCred.entrySet()) {
//				if(conn.getValue().isJsonObject() && conn.getValue().getAsJsonObject().keySet().contains("MeteringInfo")) {					
//					for(JsonElement temp :conn.getValue().getAsJsonObject().get("MeteringInfo").getAsJsonArray()) {
//						if(temp.isJsonObject()) {
//							if(!temp.getAsJsonObject().get("Type").getAsString().equalsIgnoreCase("export")) {
//								for(JsonElement e:temp.getAsJsonObject().get("Endpoints").getAsJsonArray())
//									numDownloaded++;
//							}
//						}
//					}
//				}
//			}
//		}
//		JsonObject ret = new JsonObject();
//		ret.addProperty("numDataSources", numDataSources);
//		ret.addProperty("scheduledDataSources", scheduledDataSources);
//		ret.addProperty("numDownloaded", numDownloaded);
//		ret.addProperty("numRows", numRows);
//		
//		
//    	this.template.convertAndSend("/client/message",ret.toString());
//    }
    
}
