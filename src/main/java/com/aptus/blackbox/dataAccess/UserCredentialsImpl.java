package com.aptus.blackbox.dataAccess;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.event.PushCredentials;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Repository
public class UserCredentialsImpl{
//	
//	@Value("${spring.mongodb.ipAndPort}")
//	private String mongoUrl;
//	@Value("${homepage.url}")
//	private String homeUrl;
//	@Value("${base.url}")
//	private String baseUrl;
//	@Value("${access.control.allow.origin}")
//	private String rootUrl;
//
//	@Autowired
//	private Credentials credentials;	
//	@Autowired
//	private ApplicationCredentials applicationCredentials;
//
//	private ResponseEntity<String> postCredentials(@RequestParam Map<String, String> filteredEndpoints) {
//		ResponseEntity<String> ret = null;
//		try {
//			System.out.println(filteredEndpoints.getClass());
//			System.out.println(filteredEndpoints.get("filteredendpoints") + " " + filteredEndpoints.keySet());
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("Cache-Control", "no-cache");
//			headers.add("access-control-allow-origin", rootUrl);
//			headers.add("access-control-allow-credentials", "true");
//				String endpnts = "", conId;								
//				conId = credentials.getUserId() + "_" + credentials.getCurrSrcName() + "_" + credentials.getCurrDestName() + "_"
//						+ String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
//				Gson gson = new Gson();
////				String schedule = filteredEndpoints.get("scheduled");
////				String period = filteredEndpoints.get("period");
//				String schedule = "false";
//				String period = "120";
//				JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class)
//						.getAsJsonObject().get("endpoints").getAsJsonArray();
//				ConnObj currobj = new ConnObj();
//				for (JsonElement obj : endpoints) {
//					endpnts += obj.getAsString() + ",";
//					currobj.setEndPoints(obj.getAsString());
//				}
//				currobj.setConnectionId(conId);
//				currobj.setSourceName(credentials.getCurrSrcName());
//				currobj.setDestName(credentials.getCurrDestName());
//				currobj.setPeriod((Integer.parseInt(period)*1000));
//				currobj.setScheduled(schedule);
//				credentials.setCurrConnId(currobj);
//				credentials.setConnectionIds(conId, currobj);
//				endpnts = endpnts.substring(0, endpnts.length() - 1).toLowerCase();
//				JsonObject jsonObj;
//				JsonArray endPointsArray = new JsonArray();
//				endPointsArray.add(endpnts);
//				JsonArray eachArray = new JsonArray();
//				JsonObject values = new JsonObject();
//				values.addProperty("sourceName", credentials.getCurrSrcName().toLowerCase());
//				values.addProperty("destName", credentials.getCurrDestName().toLowerCase());
//				values.addProperty("connectionId", conId.toLowerCase());
//				values.addProperty("scheduled", schedule);
//				values.addProperty("period", period);
//				values.add("endPoints", endPointsArray);
//				jsonObj = new JsonObject();
//				jsonObj.add("srcdestId", eachArray);
//				JsonObject addToSetObj = new JsonObject();
//				jsonObj.addProperty("_id", credentials.getUserId().toLowerCase());
//				String url = mongoUrl + "/credentials/" + "userCredentials";				
//				JsonObject respBody = new JsonObject();
//    			respBody.addProperty("message", "DataSource created");
//				respBody.addProperty("status", "200");
//				return new ResponseEntity<String>(respBody.toString(), headers, HttpStatus.OK);
//			
//		
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		return ret;
//	}
//	
//	private ResponseResponseEntity<String> patchCredentials(@RequestParam Map<String, String> filteredEndpoints)
//	{
//		JsonObject eachObj = new JsonObject();
//		eachObj.add("$each", eachArray);
//		jsonObj = new JsonObject();
//		jsonObj.add("srcdestId", eachObj);
//		JsonObject addToSetObj = new JsonObject();
//		addToSetObj.add("$addToSet", jsonObj);
//		url = mongoUrl + "/credentials/" + type.toLowerCase() + "Credentials/" + userId.toLowerCase();
//		met = HttpMethod.PATCH;		
//	}
//	
}
