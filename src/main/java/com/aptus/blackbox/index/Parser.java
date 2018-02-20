package com.aptus.blackbox.index;

import java.io.BufferedReader;
import java.io.FileReader;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

public class Parser {
	private String mongoUrl;
	
	public Parser(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	
	private SrcObject srcProp;
	private DestObject destProp;
	public Parser(String type)
	{
		try {
		Gson gson=new Gson();
		 ResponseEntity<String> out = null;
         RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
         HttpHeaders headers =new HttpHeaders();
         headers.add("Cache-Control", "no-cache");
         HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
         String url="http://"+mongoUrl+"/credentials"+type;
         out = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
         System.out.println(out.getBody());
		if(type.toLowerCase().startsWith("source")){
			srcProp = gson.fromJson(out.getBody(), SrcObject.class);
		}
		else if(type.toLowerCase().startsWith("destination")){
			destProp = gson.fromJson(out.getBody(), DestObject.class);
		}		
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	public SrcObject getSrcProp()
	{
		return this.srcProp;
	}
	public DestObject getDestProp()
	{
		return this.destProp;
	}
}
