package com.aptus.blackbox.controller;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.ConnObj;
import com.aptus.blackbox.index.SrcObject;
import com.aptus.blackbox.index.UrlObject;
import com.aptus.blackbox.utils.Utilities;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS,value="session")
public class SourceController {
	
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

	private String refresh;
	private UrlObject accessCode, accessToken, requestToken, refreshToken, validateCredentials;
	private List<UrlObject> endPoints;

	@RequestMapping(method = RequestMethod.GET, value = "/authsource")
	private ResponseEntity<String> source(HttpSession session) {
		ResponseEntity<String> ret = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
            headers.add("access-control-allow-credentials", "true");
			if(Utilities.isSessionValid(session,credentials)) {
				SrcObject obj = init();
				if (obj.getSteps().compareTo("TWO") == 0) {
					ret = code(accessCode);
				} else if (obj.getSteps().compareTo("THREE") == 0) {
					ret = Utilities.token(requestToken,credentials.getSrcToken());
					saveValues(ret);
					ret = code(accessCode);
				}
			}
			else {
				System.out.println("Session expired!");
				String url=baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.MOVED_PERMANENTLY);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.source");
		}
		return ret;
	}
	private SrcObject init() {			
		SrcObject obj = credentials.getSrcObj();
		System.out.println(obj.getName());
		refresh = obj.getRefresh();
		accessCode = obj.getAccessCode();
		System.out.println("source : " + accessCode.getUrl());
		accessToken = obj.getAccessToken();
		System.out.println("source : " + accessToken.getUrl());
		requestToken = obj.getRequestToken();
		System.out.println("source : " + requestToken.getUrl());
		refreshToken = obj.getRefreshToken();
		System.out.println("source : " + refreshToken.getUrl());
		validateCredentials = obj.getValidateCredentials();
		System.out.println("source : " + validateCredentials.getUrl());
		endPoints = obj.getEndPoints();
		System.out.println(endPoints.toString());
		return obj;
	}
	
	@RequestMapping(value="/deletedatasource")
	private ResponseEntity<String> deleteDataSource(@RequestParam("connids") String connids,HttpSession session){
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/createdatasource")
	private ResponseEntity<String> createDataSource(@RequestParam("filteredendpoints") String filteredEndpoints,HttpSession session)
	{		
		ResponseEntity<String> ret = null;
		//System.out.println(srcname+" "+destname);
		try	{
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if(Utilities.isSessionValid(session,credentials)) {
				if(validateCredentials==null||endPoints==null||refreshToken==null) {
					init();
				}			
				String body="",b1="",endpnts="",conId;
				conId=credentials.getUserId()+"_"+credentials.getSrcName()+"_"+credentials.getDestName()
						+"_"+String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
				for(Map.Entry<String,String> mp:credentials.getSrcToken().entrySet()) {
					b1+="{\"key\":\""+String.valueOf(mp.getKey())+"\",\"value\":\""+String.valueOf(mp.getValue())+"\"},";
				}
				Gson gson =new Gson();
				JsonArray endpoints = gson.fromJson(filteredEndpoints, JsonElement.class).getAsJsonObject().get("endpoints").getAsJsonArray();
				ConnObj currobj = new ConnObj();
				for(JsonElement obj:endpoints) {
					endpnts+="\""+obj.getAsString()+"\",";
					currobj.setEndPoints(obj.getAsString());
				}
				currobj.setConnectionId(conId);
				currobj.setSourceName(credentials.getSrcName());
				currobj.setDestName(credentials.getDestName());
				credentials.setCurrConnId(currobj);
				endpnts = endpnts.substring(0, endpnts.length()-1).toLowerCase();
				System.out.println(b1);
				if(!b1.isEmpty())
					b1=b1.substring(0,b1.length()-1);			
				
				if(credentials.isUserExist())
				{
					//sourceCredentials
					body= "{\"credentials\":["+b1+"]}";
					
					postpatchMetaData(body,"source","PATCHapp");
					
					//userCredentials
					body = "{\"$addToSet\":"
							+ "{\"srcdestId\":"
							+ "{\"$each\":["
							+ "{\"sourceName\": \""+credentials.getSrcName().toLowerCase()+"\","
							+ "\"destName\":\""+credentials.getDestName().toLowerCase()+"\","
							+ "\"connectionId\": \""+conId.toLowerCase()+"\","
							+ "\"endPoints\":["+endpnts+"]}"
							+ "]}}}";
					
					postpatchMetaData(body,"user","PATCH");
					
					//destCredentials
					body= "{\"credentials\":["+b1+"]}";
					
					postpatchMetaData(body,"destination","PATCHapp");
					
				}
				else {
					//userCredentials
					body = "{ \"_id\" : \""+credentials.getUserId().toLowerCase()+"\","
							+ "\"srcdestId\" : [ "
							+ "{ \"sourceName\" : \""+credentials.getSrcName().toLowerCase()+"\" , "
							+ "\"destName\" : \""+credentials.getDestName().toLowerCase()+"\","
							+ "\"connectionId\":\""+conId.toLowerCase()+"\","
							+ "\"endPoints\":["+endpnts+"]}"
							+ "]}";
					
					postpatchMetaData(body,"user","POST");
					
					//sourceCredentials
					body= "{\n" + 
							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
							+"_"+credentials.getSrcName().toLowerCase()+"\",\n" + 
							"	\"credentials\":["+b1+"]\n" +
							"}";
					 
					postpatchMetaData(body,"source","POST");
					
					//destCredentials
					body= "{\n" + 
							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
							+"_"+credentials.getDestName().toLowerCase()+"\",\n" +
							"				\"credentials\":["+b1+"]\n" + 
							"}";
					
					postpatchMetaData(body,"destination","POST");
				}
				//ret = data(credentials.getSrcName());
				//System.out.println(ret.getBody());
				String url=baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("",headers ,HttpStatus.OK);
			}
			else {
				System.out.println("Session expired!");
				String url=baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.OK);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	private void postpatchMetaData(String body,String type,String method)
	   {
	       try {
	           ResponseEntity<String> out = null;
	           String url="";
	           String appname = "";
	           RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
	           HttpMethod met=null;
	           System.out.println(body);
	           String filter="";
	           if(method.equalsIgnoreCase("POST")) {
	               url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials";
	               met = HttpMethod.POST;
	           }                
	           else if(method.equalsIgnoreCase("PATCH")) {
	               url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase();
	               met = HttpMethod.PATCH;
	           }
	           else if(method.equalsIgnoreCase("PATCHapp")) {
	        	   if(type.equalsIgnoreCase("source")) {
	        		   appname = credentials.getSrcName();
	        	   }
	        	   else {
	        		   appname = credentials.getDestName();
	        	   }
	        	   url =mongoUrl+"/credentials/"+type.toLowerCase()+"Credentials/"+credentials.getUserId().toLowerCase()+"_"+appname.toLowerCase();
	        	   met = HttpMethod.PATCH;
	           }
	           System.out.println(url+"\n"+met);
	           HttpHeaders headers =new HttpHeaders();
	           headers.add("Content-Type","application/json") ;
	           headers.add("Cache-Control", "no-cache");
	           headers.add("access-control-allow-origin", rootUrl);
	            headers.add("access-control-allow-credentials", "true");
	           HttpEntity<?> httpEntity = new HttpEntity<Object>(body,headers);
	           out = restTemplate.exchange(url, met, httpEntity, String.class,filter);
	           if (out.getStatusCode().is2xxSuccessful()) {
	               credentials.setUserExist(true);
	            }
	       }
	       catch(Exception e) {
	           e.printStackTrace();
	           System.out.println("source.postpatchmetadata");
	       }
	       return;    
	   }
	
        
	private ResponseEntity<String> code(UrlObject object) {
		ResponseEntity<String> redirect = null;
		HttpHeaders headers;
		try {
			String url = Utilities.buildUrl(object, credentials.getSrcToken());

			System.out.println(object.getLabel() + " = " + url);

			headers = Utilities.buildHeader(object, credentials.getSrcToken());
			headers.setLocation(URI.create(url));
			HttpEntity<?> httpEntity;
			if (object.getResponseString()!=null&&!object.getResponseString().isEmpty()) {
				String body = object.getResponseString();
				redirect = new ResponseEntity<String>(body,headers, HttpStatus.MOVED_PERMANENTLY);
			}else {
				redirect = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			}			
			System.out.println("redirect =" + redirect);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.code");
		}

		return redirect;
	}

	@RequestMapping(value = "/oauth2/s2")
	@ResponseStatus(value = HttpStatus.OK)
	private ResponseEntity<String> handlefooo(@RequestParam Map<String, String> parameters) {
		credentials.getSrcToken().putAll(parameters);
		System.out.println("token : " + credentials.getSrcToken().keySet() + ":" + credentials.getSrcToken().values());
		System.out.println("parameters : " + parameters.keySet() + ":" + parameters.values());
		ResponseEntity<String> out = null;
		try {
			String url = Utilities.buildUrl(accessToken, credentials.getSrcToken());
			System.out.println(accessToken.getLabel() + " = " + url);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = Utilities.buildHeader(accessToken, credentials.getSrcToken());
			HttpEntity<?> httpEntity;
			if (accessToken.getResponseString()!=null && !accessToken.getResponseString().isEmpty()) {
				httpEntity = new HttpEntity<Object>(accessToken.getResponseString(), headers);
			} else if (accessToken.getResponseBody()!=null && !accessToken.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> body = Utilities.buildBody(accessToken, credentials.getSrcToken());
				httpEntity = new HttpEntity<Object>(body, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (accessToken.getMethod() == "GET") ? HttpMethod.GET : HttpMethod.POST;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			 url =UrlEscapers.urlFragmentEscaper().escape(url);
			out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
			saveValues(out);
			out = Utilities.token(validateCredentials,credentials.getSrcToken());
			//System.out.println(out.getBody());
			headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if (!out.getStatusCode().is2xxSuccessful()) {
				System.out.println("invalid access token");
				Utilities.invalid();
				credentials.setSrcValid(false);				
				url=homeUrl;
				headers.setLocation(URI.create(url+"/close.html"));
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);

			} else {
				Utilities.valid();
				credentials.setSrcValid(true);
				url=homeUrl;
				headers.setLocation(URI.create(url+"/close.html"));
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.s2");
		}
		return null;
	}

	private void saveValues(ResponseEntity<String> out) {
		if (out.getBody() != null) {
			try {
				credentials.getSrcToken().putAll(new Gson().fromJson(out.getBody(), HashMap.class));
			} catch (Exception e) {
				for (String s : out.getBody().toString().split("&")) {
					System.out.println(s);
					credentials.getSrcToken().put(s.split("=")[0], s.split("=")[1]);
				}
			}
			System.out.println("token : " + credentials.getSrcToken().keySet() + ":" + credentials.getSrcToken().values());
		}
	}
}