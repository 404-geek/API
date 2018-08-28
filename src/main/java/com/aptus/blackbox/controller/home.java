package com.aptus.blackbox.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.ThreadContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.BlackBoxReloadedApp;
import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataInterfaces.SrcDestListDAO;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.dataServices.MeteringService;
import com.aptus.blackbox.dataServices.SchedulingService;
import com.aptus.blackbox.dataServices.SrcDestCredentialsService;
import com.aptus.blackbox.dataServices.SrcDestListService;
import com.aptus.blackbox.dataServices.UserConnectorService;
import com.aptus.blackbox.dataServices.UserInfoService;
import com.aptus.blackbox.datamodels.SrcDestCredentials;
import com.aptus.blackbox.datamodels.SrcDestList;
import com.aptus.blackbox.datamodels.UserConnectors;
import com.aptus.blackbox.datamodels.UserInfo;
import com.aptus.blackbox.datamodels.Metering.ConnectionMetering;
import com.aptus.blackbox.index.ScheduleInfo;
import com.aptus.blackbox.models.ConnObj;
import com.aptus.blackbox.models.ResponseObject;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.security.ExceptionHandling;
import com.aptus.blackbox.utils.Constants;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*
 * "pagination":[
					{"key":"info.page","to-add":"page","value":"inc"},
 					{"key":"info.page","to-add":"","value":"url"}, 
					{"key":"info.page","to-add":"page","value":"append"}, 
					{"key":"info.page","to-add":"page","value":"self-inc"}, 
 					{"key":"info.page","to-add":"page","value":"self-inc"} 
				]
 */

@Controller
public class home extends RESTFetch{
	
	@Autowired
	private SrcDestListService srcdestlistService;
	@Autowired
	private Credentials credentials;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private Config config;
	
	@Autowired
	private UserInfoService userInfoService;
	
	@Autowired
	private UserConnectorService userConnectorService;


	final Logger logger = LogManager.getLogger(home.class.getPackage());

	@Autowired
	private MeteringService meteringService;

	@Autowired
	private SrcDestCredentialsService srcDestCredentialsService;
	
	@Autowired
	private SchedulingService schedulingService;
	
	
	

	
	
	@RequestMapping("/log")
	private ResponseEntity<String> dfs() {
		
		System.out.println(credentials.getSrcObj()+" TOKEN == "+credentials.getSrcToken());
		List<Map<String,String>> mcred = new ArrayList<Map<String,String>>();
		
		for (Map.Entry<String, String> mp : credentials.getSrcToken().entrySet()) {
			
			
			Map<String, String > map = new HashMap<String,String>();
			map.put("key", String.valueOf(mp.getKey()));
			map.put("value", String.valueOf(mp.getValue()));
			mcred.add(map);
			
			
		}
		SrcDestCredentials srcCredentials  = new SrcDestCredentials();
		srcCredentials.setCredentialId(credentials.getUserId().toLowerCase() + "_" + credentials.getCurrSrcName().toLowerCase());
		srcCredentials.setCredentials(mcred);
		System.out.println(srcCredentials);
		srcDestCredentialsService.insertCredentials(srcCredentials, "sourceCredentials");
		
		
		return null;
	}
	
	@RequestMapping(value="/login")
	private ResponseEntity<String> login(@RequestParam("userId") String _id,@RequestParam("password") String password,HttpSession session )
	{
		System.out.println("/login session"+session.getId());
		JsonObject response = new JsonObject();
		
		if(!userInfoService.userExist(_id)) {
			response = new ResponseObject().Response(Constants.USER_NOT_FOUND_CODE, Constants.USER_NOT_FOUND_MSG, _id);
		}
		
		else if(!userInfoService.userValid(_id,password)) {
			response = new ResponseObject().Response(Constants.INVALID_CREDENTIALS_CODE, Constants.INVALID_CREDENTIALS_MSG, _id);
		}
		
		else { 
			
			response = new ResponseObject().Response(Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, _id);
			credentials.setUserId(_id);
			applicationCredentials.setSessionId(_id, session.getId());
			getConnectionIds(session);
//			
//			ThreadContext.clearAll();
//			 ThreadContext.put("id", "192.168.21.9");
//			logger.info("User success login");
		}
		return ResponseEntity.status(HttpStatus.OK).headers(null).body(response.toString());
	}
	
	@RequestMapping(value="/activeUsers")
	private ResponseEntity<String> getActiveUsers()
	{
		ThreadContext.put("id", "poupopuop");
		logger.error("Helllo worls");
		JsonObject jobj;
		try {
			jobj = new JsonObject();
			System.out.println("Users Currently Active");
			applicationCredentials.getSessionId().forEach((k,v)->{
				System.out.println(k+":"+v);
				jobj.addProperty(k, v);
			});
			return ResponseEntity.status(HttpStatus.OK).headers(null).body(jobj.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
		

	
	@RequestMapping(value="/loginOld")
	private ResponseEntity<String> loginOld(@RequestParam("userId") String user,@RequestParam("password") String pass,HttpSession session )
	{
		logger.info("-------  user login on process ------ ");
		try {
		
			logger.info("User login success");
			System.out.println(user);		
			System.out.println("/inside login");		
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
            headers.add("access-control-allow-credentials", "true");
            headers.add("Authorization", "Basic YTph");
			JsonObject respBody = new JsonObject();
			ResponseEntity<String> ret=null;
			ret = existUser(user,"userInfo");
 			//System.out.println(new Gson().fromJson(existUser(user,"userInfo").getBody(),JsonObject.class).getAsJsonObject().get("code"));
			if(new Gson().fromJson(ret.getBody(),JsonObject.class).getAsJsonObject().get("code").getAsString().equals("200")){
				System.out.println("inside login");
				String url = config.getMongoUrl()+"/credentials/userInfo/"+user;
				System.out.println(url);
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
				JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
				applicationCredentials.setSessionId(user,session.getId());
//				if(!obj.get("password").toString().equals(pass)) {
//					respBody.addProperty("id", user);
//					respBody.addProperty("status", "404");
//					return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers).body(respBody.toString());
//				}
				applicationCredentials.setApplicationCred(user, new ScheduleInfo());
				applicationCredentials.getApplicationCred().get(user).setLastAccessTime(session.getLastAccessedTime());

				if(new Gson().fromJson((existUser(user, "userCredentials").getBody()),JsonObject.class).getAsJsonObject().get("code").getAsString().equals("200"))
					getConnectionIds(session);
				
				System.out.println(session.getId());
				
				respBody.addProperty("id", user);
				respBody.addProperty("status", "200");
				System.out.println("inside if");
				//throw new Handling("Some Exception");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				
				/*respBody.addProperty("id", user);
				respBody.addProperty("status", "404");
				System.out.println(respBody.toString());
				*/
				//return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
				return ret;
			}
		}
		
	/*	catch (Exception e) {
			//ExceptionHandling exceptionHandling=new ExceptionHandling()
			// TODO Auto-generated catch block
			System.out.println("inside home handling catch");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.OK).body(null);
			
		}*/
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside login catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);

			//System.out.println(out.getStatusCode().toString());
			return out;
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		
		//return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	

	@RequestMapping(value="/signup",method = RequestMethod.POST)
	private ResponseEntity<String> signup(@RequestBody UserInfo user)
	{
	  System.out.println(user);
	  
	  JsonObject response = null ;
	  try {
		if(userInfoService.userExist(user.getUserId())) {
			  System.out.println("User ID Exists "+user.getUserId());
			  
			  response = new ResponseObject()
					  .Response( Constants.USER_EXIST_CODE, Constants.USER_EXIST_MSG, user.getUserId());
		  }
		  else {
			  applicationCredentials.setApplicationCred(user.getUserId(), new ScheduleInfo());
			  System.out.println("User ID Not Exists "+user.getUserId());
			  
			  userInfoService.createUser(user);
			  userConnectorService.createUser(user.getUserId());
			  meteringService.createUser(user.getUserId());
			  schedulingService.createUser(user.getUserId());
			  
			  response = new ResponseObject()
					  .Response( Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, user.getUserId());
		  }
	} catch (Exception e) {
		e.printStackTrace();
	}
	  return ResponseEntity.status(HttpStatus.OK).headers(null).body(response.toString());
	}
	
	
	
	@RequestMapping(value="/signupOld")
	private ResponseEntity<String> signupOld(@RequestParam HashMap<String,String> params)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {			
			System.out.println(params);	
			String userId=params.get("_id");
			JsonObject respBody = new JsonObject();			
        	headers.add("Content-Type", "application/json");
			if(existUser(userId,"userInfo").getBody().contains("200")){			
				System.out.println("User ID Exists");
				respBody.addProperty("id", userId);
				respBody.addProperty("status", "61");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				System.out.println("User ID Not Exists");
				JsonObject body = new JsonObject();
				for(Map.Entry<String, String> entry : params.entrySet()) {
				    body.addProperty(entry.getKey(),entry.getValue());
				}				
				ResponseEntity<String> out = null;
				String url = "";				
				RestTemplate restTemplate = new RestTemplate();
				url = config.getMongoUrl() + "/credentials/userInfo";				
				System.out.println(url);
				HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(),headers);
				out = restTemplate.exchange(url, HttpMethod.POST , httpEntity, String.class);
				if (out.getStatusCode().is2xxSuccessful()) {
					System.out.println("Pushed successfully!");
				}				
				respBody.addProperty("id",userId);
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		} 
		catch(HttpClientErrorException e) {
            JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Error");
            respBody.addProperty("status", "404");
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
        }
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	
	@RequestMapping(value="/update1")
	private ResponseEntity<String> update1(@RequestBody UserInfo user)
	{
		JsonObject response = null ;
		if(userInfoService.userExist(user.getUserId())) {
			  System.out.println("User ID Exists "+user.getUserId());
			  response = new ResponseObject()
					  .Response( Constants.USER_EXIST_CODE, Constants.USER_EXIST_MSG, user.getUserId());
		  }
		  else {
			 
			  System.out.println("User ID Not Exists "+user.getUserId());
			//  userInfoService.updateUser(user);
			  response = new ResponseObject()
					  .Response( Constants.SUCCESS_CODE, Constants.SUCCESS_MSG, user.getUserId());
		  }
		return ResponseEntity.status(HttpStatus.OK).headers(null).body(response.toString());
	}
	
	@RequestMapping(value="/update")
	private ResponseEntity<String> update(@RequestParam HashMap<String,String> params,HttpSession session)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Content-Type", "application/json");
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
			System.out.println("updating" + params);			
			JsonObject respBody = new JsonObject();
			String userId=params.get("_id").toString();
			String url = config.getMongoUrl()+"/credentials/userInfo/"+ userId;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			JsonObject body = new JsonObject();

			for(Map.Entry<String, String> entry : params.entrySet()) {
			    body.addProperty(entry.getKey(),entry.getValue());
			}
			HttpHeaders header = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(),headers);
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			ResponseEntity<String> out = restTemplate.exchange(uri, HttpMethod.PATCH, httpEntity,String.class);
				
			JsonObject obj = new Gson().fromJson(out.getBody(), JsonObject.class);
			System.out.println(session.getId());
			respBody.addProperty("id", userId);
			respBody.addProperty("status", "200");
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else{
				session.invalidate();
				System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).headers(headers).body(respBody.toString());
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		//store in credentials
	}
	
	
	/* Input:user_id
	 * Takes user_id as input, checks if user already exists and stores true/false accordingly in credentials.
	 * Return type: void 
	 */
	private ResponseEntity<String> existUser(String userId,String type) {
		ResponseEntity<String> out = null;
		try {
			boolean ret=false;
			credentials.setUserId(userId);
			
			System.out.println("LOGGGGGIIINNN");
			  ThreadContext.put("id", "192.168.21.9loginn");
			  logger.info("asfdsadasfdf");
			
			
			RestTemplate restTemplate = new RestTemplate();
			//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			String filter = "{\"_id\":\"" + credentials.getUserId().toLowerCase() + "\"}";
			String url;
			url = config.getMongoUrl()+"/credentials/"+type+"?filter=" + filter;
			System.out.println(url);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			HttpHeaders headers = new HttpHeaders();
			// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
			headers.add("Cache-Control", "no-cache");
			headers.add("Authorization", "Basic YTph");
			headers.add("access-control-allow-origin", config.getRootUrl());
            headers.add("access-control-allow-credentials", "true");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
			System.out.println("inside existuser");
			
			JsonElement jelem = new Gson().fromJson(out.getBody(), JsonElement.class);
			JsonObject jobj = jelem.getAsJsonObject();
			ret=jobj.get("_returned").getAsInt() == 0 ? false : true;
			if(type.equalsIgnoreCase("usercredentials"))
				credentials.setUserExist(ret);
			
			//System.out.println(ret);
			JsonObject respBody = new JsonObject();
			if(ret)
			{
				 respBody.addProperty("code", "200");
				 respBody.addProperty("message", "User found");
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}
			else
			{
				 respBody.addProperty("code", "404");
				 respBody.addProperty("message", "User not found");
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}
		}

		
		
				
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside exituser catch");
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			//System.out.println(out.getBody());
			//System.out.println(out.getStatusCode().toString());
			return out;//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdest")
	private ResponseEntity<String> getSrcDest1(HttpSession session) {
		System.out.println("/getsrcdest session"+session.getId());
		ResponseEntity<String> s=null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				String response = srcdestlistService.getSrcDestList();
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
			}else {
				session.invalidate();
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}
	catch (HttpStatusCodeException e) {
			
			System.out.println("Inside getsrcdest catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			
			//System.out.println(out.getStatusCode().toString());
			return out;
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
		
	
	@RequestMapping(method = RequestMethod.GET, value = "/getsrcdestOld")
	private ResponseEntity<String> getSrcDestOld(HttpSession session) {
		System.out.println("INSIDE /getsrcdst");
		ResponseEntity<String> s=null;
		HttpHeaders headers = new HttpHeaders();
		// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Authorization", "Basic YTph");
		try {
			
			System.out.println(session.getId());			
			if(session.getId()==applicationCredentials.getSessionId(credentials.getUserId())) {
				String name;
				RestTemplate restTemplate = new RestTemplate();
				String url = config.getMongoUrl()+"/copy_credentials/SrcDstlist/srcdestlist";
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				s  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(s.getBody().toString());
			}
			else {
				session.invalidate();
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}
		
		
		catch (HttpStatusCodeException e) {
			
			System.out.println("Inside getsrcdest catch");
			ResponseEntity<String> out = null;
			e.getStatusCode();
			
			ExceptionHandling exceptionhandling=new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			
			//System.out.println(out.getStatusCode().toString());
			return out;
			//ResponseEntity.status(HttpStatus.OK).body(null);
			
		}
		
		
		catch(Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	
	@RequestMapping(value="/filterendpoints")
	private ResponseEntity<String> filterendpoints(HttpSession session)
	{
		
		System.out.println("INSIDE /filterendpoints");
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
		headers.add("access-control-allow-credentials", "true");
		try {			
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {

				JsonObject jobj = new JsonObject();
				JsonArray catagory = new JsonArray();
				JsonObject temp = new JsonObject();
				JsonArray endpoints=new JsonArray();
				boolean flag1 = false;
				for(UrlObject obj:credentials.getSrcObj().getDataEndPoints()) {
					if(obj.getCatagory().equalsIgnoreCase("others")) {
						endpoints.add(obj.getLabel());
						flag1=true;
					}
				}
				for(UrlObject obj:credentials.getSrcObj().getInfoEndpoints()) {
					endpoints.add(obj.getLabel());
					flag1=true;
				}
				if(flag1==true) {
					temp.add("value", endpoints);
					temp.addProperty("name", "others");
					temp.addProperty("key", "Others");
					catagory.add(temp);
				}				
		        List<String> list;
		        Gson gson = new Gson();
				for(UrlObject obj:credentials.getSrcObj().getImplicitEndpoints()) {
					temp = new JsonObject();
					ResponseEntity<String> data = Utilities.token(obj, credentials.getSrcToken(), "filteredEndpoints");					
					list = new ArrayList<String>();
					list = Utilities.checkByPath(obj.getData().split("::"), 0, new Gson().fromJson(data.getBody(),JsonElement.class), list);
					temp.add("value", gson.fromJson(gson.toJson(list),JsonArray.class));
					temp.addProperty("name", obj.getCatagory());
					temp.addProperty("key", obj.getLabel());
					catagory.add(temp);
				}
				jobj.add("endpoints", catagory);
				jobj.addProperty("status", "200");
				System.out.println(jobj.toString());
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobj.toString());
			}
			else
			{
				session.invalidate();
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	
	

	
	@RequestMapping(value="/getconnectionidsOld")
	private ResponseEntity<String> getConnectionIdsOld(HttpSession session) {
		System.out.println(applicationCredentials.getSessionId(credentials.getUserId())+"INSIDE /getconnectionids:" +session.getId());
		String dataSource=null;
		HttpHeaders headers = new HttpHeaders();			
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Authorization", "Basic YTph");
		try {
			
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				ResponseEntity<String> out = null;
				RestTemplate restTemplate = new RestTemplate();
				//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				String url = config.getMongoUrl()+"/credentials/userCredentials/"+credentials.getUserId();
				URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				HttpHeaders header = new HttpHeaders();
				// headers.add("Authorization","Basic YWRtaW46Y2hhbmdlaXQ=");
				header.add("Cache-Control", "no-cache");
				HttpEntity<?> httpEntity = new HttpEntity<Object>(header);
				out = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,String.class);
				
				Gson gson=new Gson();								
				JsonObject respBody = new JsonObject();
				respBody.addProperty("status", "200");
				respBody.add("data",gson.fromJson(out.getBody(), JsonElement.class));
				
				ConnObj conObj = new ConnObj();
				JsonElement data = gson.fromJson(out.getBody(), JsonElement.class);
				JsonArray srcdestId = data.getAsJsonObject().get("srcdestId").getAsJsonArray();
				for(JsonElement ele:srcdestId) {
					conObj = gson.fromJson(ele, ConnObj.class);
					credentials.setConnectionIds(conObj.getConnectionId(), conObj);
				}
				
			    url = config.getMongoUrl()+"/copy_credentials/SrcDstlist/srcdestlist";
			    uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				out  = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
				respBody.add("images",gson.fromJson(out.getBody(), JsonElement.class));				
				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else {
				session.invalidate();
   				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}			
		}
		catch(HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if(e.getMessage().startsWith("4")) {
	            JsonObject respBody = new JsonObject();
				respBody.addProperty("data", "null");
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}

	@RequestMapping(value="/getconnectionids")
	private ResponseEntity<String> getConnectionIds(HttpSession session) {
		String dataSource=null;
		HttpHeaders headers = new HttpHeaders();			
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        headers.add("Authorization", "Basic YTph");
		try {
			
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				ResponseEntity<String> out = null;
				
				//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			
				UserConnectors connObj = userConnectorService.getConnectorObjects(credentials.getUserId());
				
				for(ConnObj ele:connObj.getConnectorObjs()) 
					credentials.setConnectionIds(ele.getConnectionId(), ele);
				
				JsonObject respBody = new JsonObject();
				respBody.addProperty("status", "200");
				respBody.add("data",new Gson().fromJson(new Gson().toJson(connObj,UserConnectors.class),JsonElement.class));
				respBody.add("images",new Gson().fromJson(srcdestlistService.getSrcDestList(),JsonElement.class));				
				
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
			else {
				session.invalidate();
   				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}			
		}
		catch(HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if(e.getMessage().startsWith("4")) {
	            JsonObject respBody = new JsonObject();
				respBody.addProperty("data", "null");
				respBody.addProperty("status", "200");
				return ResponseEntity.status(HttpStatus.OK).headers(headers).body(respBody.toString());
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	
	/*@RequestMapping("/OLDstatistics")
	private ResponseEntity<String> OLDstatistics(HttpSession session){
		
		HttpHeaders headers = new HttpHeaders();			
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
        JsonObject jobj = new JsonObject();
		try {
			JsonObject jobj1 = new JsonObject();
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
			    long n;
				if(session.getAttribute("ss")==null)
					{
						n=(long)(Math.random()*(9999-1000))+6000;
						session.setAttribute("ss", n);
					}
				else
					{
						n = Long.parseLong(session.getAttribute("ss").toString());
						session.setAttribute("ss", n+37 );
					}
			
			jobj1.addProperty("DataSources Created",n);
			jobj1.addProperty("DataSources Scheduled", n-1279);
			jobj1.addProperty("Files Downloaded", n-3570);
			jobj1.addProperty("Rows Fetched", n+1236789);
			
				
			}else {
				session.invalidate();
					System.out.println("Session expired!");
				JsonObject respBody = new JsonObject();
				respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
			
			jobj.addProperty("code", "200");
			jobj.addProperty("message", "Statistics data updated");
			jobj.add("data",jobj1);
			return ResponseEntity.status(HttpStatus.OK).headers(headers).body(jobj.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
		
		
	}
*/	
		
}