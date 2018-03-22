package com.aptus.blackbox.controller;

import java.net.URI;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.ApplicationCredentials;
import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.event.InterruptThread;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
public class UITrigger {

	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;
	
	@Autowired
	private Credentials credentials;	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	
	@RequestMapping("/getScheduledStatus")
	public ResponseEntity<Object> getScheduledStatus(HttpSession session) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", rootUrl);
		headers.add("access-control-allow-credentials", "true");
		try {
			if (Utilities.isSessionValid(session, credentials)) {
				
				String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
				String url = mongoUrl + "/credentials/scheduledStatus?filter=" + filter;

				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();

				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				JsonObject respBody = new JsonObject();
				
				if(obj.get("_returned").getAsInt() == 0 ? false : true)
				{
					respBody.add("data",obj);
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Exist");
				}else {
					respBody.addProperty("data","null");
					respBody.addProperty("status", "200");
					respBody.addProperty("message", "Scheduled Data Not Exist");
				}
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			} else {
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers)
						.body(respBody.toString());
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		// store in credentials
	}
	
//	@RequestMapping("/togglescheduling")
//	public ResponseEntity<String> toggleScheduling(@RequestParam("connid") String connId,
//			@RequestParam("toggle") String toggle,HttpSession session){
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Cache-Control", "no-cache");
//		headers.add("access-control-allow-origin", rootUrl);
//		headers.add("access-control-allow-credentials", "true");
//		if (Utilities.isSessionValid(session, credentials)) {
//			if(toggle.equalsIgnoreCase("on")) {
//				boolean ret=false;
//				ResponseEntity<String> out = null;
//				RestTemplate restTemplate = new RestTemplate();
//				//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
//				String filter = "filter={\"_id\":\""+credentials.getUserId()+"\"}&filter={\""+connId+"\":{\"$exists\":true,\"$ne\":null}}";
//				String url = mongoUrl+"/credentials/scheduledStatus?" + filter;
//				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
//				HttpHeaders header = new HttpHeaders();
//				// header.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
//				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
//				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
//				System.out.println(out.getBody());
//				JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
//				JsonObject jobj = jelem.getAsJsonObject();
//				if(jobj.get("_returned").getAsInt() != 0) {
//					url = mongoUrl+"/credentials/scheduledStatus/"+credentials.getUserId();
//					
//				}
//			}
//			else {
//				if(applicationCredentials.getApplicationCred().get(credentials.getUserId())!=null) {
//            		if(applicationCredentials.getApplicationCred().get(credentials
//            				.getUserId()).getSchedulingObjects().get(credentials.getCurrConnId().getConnectionId())!=null) {
//            			applicationEventPublisher.publishEvent(new InterruptThread(applicationCredentials.getApplicationCred().get(credentials
//                				.getUserId()).getSchedulingObjects().get(credentials.getCurrConnId().getConnectionId()).getThread()
//            					, true, credentials.getUserId(), credentials.getCurrConnId().getConnectionId()));
//            		}            		
//            	}
//			}
//		}
//		else {
//			System.out.println("Session expired!");
//			JsonObject respBody = new JsonObject();
//			respBody.addProperty("message", "Sorry! Your session has expired");
//			respBody.addProperty("status", "33");
//			return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers)
//					.body(respBody.toString());
//		}
//	}
}
