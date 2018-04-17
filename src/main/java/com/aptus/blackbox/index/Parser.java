package com.aptus.blackbox.index;

import java.io.Serializable;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.DomainObjects.DestObject;
import com.aptus.blackbox.DomainObjects.SrcObject;
import com.google.gson.Gson;
public class Parser implements Serializable{
	Parser(){}
	private SrcObject srcProp;
	private DestObject destProp;
	public Parser(String type,String Id,String mongoUrl)
	{
		try {			
		Gson gson=new Gson();
		 ResponseEntity<String> out = null;
         RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
         HttpHeaders headers =new HttpHeaders();
         headers.add("Cache-Control", "no-cache");
         HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
         String url=mongoUrl+"/credentials/"+type+"/"+Id.toUpperCase();
         System.out.println(url);
         out = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class); 
         System.out.println(out.getBody());
         if(type.equalsIgnoreCase("source")){
			srcProp = gson.fromJson(out.getBody(), SrcObject.class);
		}
		else if(type.equalsIgnoreCase("destination")){
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
