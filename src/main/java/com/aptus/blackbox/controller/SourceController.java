package com.aptus.blackbox.controller;

import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.RESTFetch;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Config;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class SourceController extends RESTFetch {
	

	@Autowired
	private Credentials credentials;
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private Config config;


	private String refresh;
	private UrlObject accessCode, accessToken, requestToken, refreshToken, validateCredentials;
	private List<UrlObject> endPoints;

	@RequestMapping(method = RequestMethod.GET, value = "/authsource")
	private ResponseEntity<String> source(HttpSession session) {
		System.out.println("inside authsource");
		credentials.setCurrSrcValid(false);
		ResponseEntity<String> ret = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache");
		headers.add("access-control-allow-origin", config.getRootUrl());
        headers.add("access-control-allow-credentials", "true");
		try {			
			if(Utilities.isSessionValid(session,applicationCredentials,credentials.getUserId())) {
				SourceConfig obj = init();
				if (obj.getSteps().compareTo("TWO") == 0) {
					ret = code(accessCode);
				} else if (obj.getSteps().compareTo("THREE") == 0) {
					ret = Utilities
							.token(requestToken,credentials.getSrcToken(),credentials.getUserId()+"SourceController.authsource");
					saveValues(ret);
					ret = code(accessCode);
				}
				return ret;
			}
			else {
				System.out.println("Session expired!");
    			JsonObject respBody = new JsonObject();
    			respBody.addProperty("message", "Sorry! Your session has expired");
				respBody.addProperty("status", "33");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(respBody.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.source");
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).headers(headers).body(null);
	}
	private SourceConfig init() {			
		SourceConfig obj = credentials.getSrcObj();
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
		endPoints = obj.getDataEndPoints();
		System.out.println(endPoints);
		return obj;
	}	
        
	private ResponseEntity<String> code(UrlObject object) {
		ResponseEntity<String> redirect = null;
		HttpHeaders headers;
		try {
			String url = Utilities.buildUrl(object, credentials.getSrcToken(),credentials.getUserId()+"SourceController.code");

			System.out.println(object.getLabel() + " = " + url);

			headers = Utilities.buildHeader(object, credentials.getSrcToken(),credentials.getUserId()+"SourceController.code");
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			headers.setLocation(uri);
			HttpEntity<?> httpEntity;			
			redirect = new ResponseEntity<String>(headers, HttpStatus.MOVED_PERMANENTLY);
			System.out.println("redirect =" + redirect);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.code");
		}

		return redirect;
	}

	@RequestMapping(value = "/oauth2/s2")
	@ResponseStatus(value = HttpStatus.OK)
	private ResponseEntity<String> handlefooo(@RequestParam HashMap<String, String> parameters) {
		credentials.getSrcToken().putAll(parameters);
		
		System.out.println("parameters : " + parameters.keySet() + ":" + parameters.values());
		ResponseEntity<String> out = null;
		try {
			for(Entry<String, String> entry:parameters.entrySet()) {
				parameters.put(entry.getKey(),URLDecoder.decode(entry.getValue(), "UTF-8"));
			}
			String url = Utilities.buildUrl(accessToken, credentials.getSrcToken(),credentials.getUserId()+"SourceController.handlefooo");
			System.out.println(accessToken.getLabel() + " = " + url);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = Utilities.buildHeader(accessToken, credentials.getSrcToken(),credentials.getUserId()+"SourceController.handlefooo");
			HttpEntity<?> httpEntity;
			if (!accessToken.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> preBody = Utilities.buildBody(accessToken, credentials.getSrcToken(),"SourceController.handlefooo");
				Object postBody=null;
				for(objects head:accessToken.getHeader())
				{
					if(head.getKey().equalsIgnoreCase("content-type")) {
						postBody=Utilities.bodyBuilder(head.getValue(),preBody,"SourceController.handlefooo");
						break;
					}
				}
				httpEntity = new HttpEntity<Object>(postBody, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
//			if (accessToken.getResponseString()!=null && !accessToken.getResponseString().isEmpty()) {
//				httpEntity = new HttpEntity<Object>(accessToken.getResponseString(), headers);
//			} else if (accessToken.getResponseBody()!=null && !accessToken.getResponseBody().isEmpty()) {
//				MultiValueMap<String, String> body = Utilities.buildBody(accessToken, credentials.getSrcToken(),credentials.getUserId()+"SourceController.handlefooo");
//				httpEntity = new HttpEntity<Object>(body, headers);
//			} else {
//				httpEntity = new HttpEntity<Object>(headers);
//			}
			HttpMethod method = (accessToken.getMethod() == "GET") ? HttpMethod.GET : HttpMethod.POST;
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			out = restTemplate.exchange(uri, method, httpEntity, String.class);
			saveValues(out);
			out = Utilities.token(validateCredentials,credentials.getSrcToken(),credentials.getUserId()+"SourceController.handlefooo");
			
			System.out.println(out);
			headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", config.getRootUrl());
			headers.add("access-control-allow-credentials", "true");
			if (!out.getStatusCode().is2xxSuccessful()) {
				System.out.println("invalid access token");
				Utilities.invalid();
				credentials.setCurrSrcValid(false);				
				url="/close.html";
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				headers.setLocation(uri);
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);

			} else {
				Utilities.valid();
				credentials.setCurrSrcValid(true);
				url="/close.html";
				uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
				headers.setLocation(uri);
				return new ResponseEntity<String>("",headers ,HttpStatus.MOVED_PERMANENTLY);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.s2");
		}
		return null;
	}

	public void saveValues(ResponseEntity<String> out) {
		if (out.getBody() != null) {
			try {
				credentials.getSrcToken().putAll(new Gson().fromJson(out.getBody().replace("\\", ""), HashMap.class));
			
				
			} catch (Exception e) {
				for (String s : out.getBody().toString().split("&")) {
					System.out.println(s);
					credentials.getSrcToken().put(s.split("=")[0], s.split("=")[1]);
				}
			}
			
		}
	}
}