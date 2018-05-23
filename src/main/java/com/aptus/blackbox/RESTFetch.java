package com.aptus.blackbox;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;
import com.aptus.blackbox.utils.Utilities;
import com.google.gson.JsonObject;

public abstract class RESTFetch extends SourceAuthorization {
	
	protected ResponseEntity<String> token(UrlObject object,Map<String, String> values,String message) {
		ResponseEntity<String> out = null;
		try {
			RestTemplate restTemplate = new RestTemplate();
			String url = buildUrl(object, values,message);
			System.out.println(message+" "+object.getLabel() + " = " + url);

			HttpHeaders headers = buildHeader(object, values,message);
			HttpEntity<?> httpEntity;
			if (!object.getResponseBody().isEmpty()) {
				MultiValueMap<String, String> preBody = buildBody(object, values,"");
				Object postBody=null;
				for(objects head:object.getHeader())
				{
					if(head.getKey().equalsIgnoreCase("content-type")) {
						postBody=bodyBuilder(head.getValue(),preBody,message);
						break;
					}
				}
				httpEntity = new HttpEntity<Object>(postBody, headers);
			} else {
				httpEntity = new HttpEntity<Object>(headers);
			}
			HttpMethod method = (object.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
			System.out.println(message+" "+"Method : "+method);
			URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
			System.out.println(message+" "+"----------------------------"+uri);
			out = restTemplate.exchange(uri, method, httpEntity, String.class);
			//System.out.println(out.getBody());
		} 
		catch(HttpClientErrorException e) {
			System.out.println(e.getMessage());
			if(!e.getMessage().startsWith("2")) {
				out =  new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		}
		catch (Exception e) {
			e.printStackTrace();

			System.out.println("\nutilities.token");
		}
		return out;
	}
	
	protected Object bodyBuilder(String contentType, MultiValueMap<String, String> preBody,String message) throws Exception {
		switch(contentType){
		case("application/json"):
			return JsonBuilder(preBody,message);
		case("application/xml"):
			return XmlBuilder(preBody,message);
		case("text/xml"):
			return XmlBuilder(preBody,message);
		case("text/html"):
			return HtmlBuilder(preBody,message);
		case("application/x-www-form-urlencoded"):
			return preBody;
		default:
			throw new Exception("Unsupported Content Type");
		}
	}
	
	protected String HtmlBuilder(MultiValueMap<String, String> preBody, String message) {
		try {
			String body = "<!DOCTYPE html>";
			for(Entry<String, List<String>> element:preBody.entrySet()) {
				body.concat("<"+element.getKey()+">"+element.getValue()+"</"+element.getKey()+">");
			}
			System.out.println(message+" : Body :"+body);
			return body;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected String XmlBuilder(MultiValueMap<String, String> preBody, String message) {
		try {
			String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
			for(Entry<String, List<String>> element:preBody.entrySet()) {
				body.concat("<"+element.getKey()+">"+element.getValue()+"</"+element.getKey()+">");
			}
			System.out.println(message+" : Body :"+body);
			return body;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected String JsonBuilder(MultiValueMap<String, String> preBody, String message) {
		try {
			JsonObject body = new JsonObject();
			for(Entry<String, List<String>> element:preBody.entrySet()) {
				body.addProperty(element.getKey(), element.getValue().get(0));
			}
			System.out.println(message+" : Body :"+body.toString());
			return body.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(message+e.getMessage());
			e.printStackTrace();			
		}
		return null;
	}
	
	protected String buildUrl(UrlObject token, Map<String, String> values,String message)
	{
		String params = "?";
		System.out.println(message+" "+"parameters = "+token.getParams());
		for(objects obj:token.getParams())
		{
			String key,value;
			key = obj.getKey();
			
			if(obj.getValue().equals("codeValue"))
				value = values.get(key);
			else
				value = obj.getValue();
			
			String s=" ";
			for(objects x:obj.getValueList())
			{
				if(x.getValue().equals("codeValue"))
					s+= x.getKey() + "=" + values.get(x.getKey())+",";
				else if(x.getValue().equals("codeValueSpace"))
					s+= values.get(x.getKey())+",";
				else
					s+= x.getKey() + "=" + x.getValue()+",";
			}
			value+= s.substring(0,s.length()-1);
			params += key+"="+value+"&";
		}
		String url= url(token.getUrl(),  values)+params.substring(0,params.length()-1);
		System.out.println(message+" "+url);
		return url;
		
	}
	protected String url(String url,Map<String, String> values) {
		String newUrl="";
		for(String comp:url.split("/")) {
			if(comp.startsWith("{")) {
				newUrl+=(values.get(comp.substring(1,comp.length()-1))+"/");
			}
			else {
				newUrl+=(comp+"/");
			}
		}
		return newUrl.substring(0,newUrl.length()-1);
	}
	protected HttpHeaders buildHeader (UrlObject token, Map<String, String> credentials,String message) throws Exception
	{
		Map<String, String> oauth; 
		System.out.println(message+" "+"::HEADERS::");
		HttpHeaders headers = new HttpHeaders();
		for(objects obj:token.getHeader())
			{
				String key,value;
				key = obj.getKey();
				
				if(obj.getValue().equals("codeValue"))
					value = credentials.get(key);
				else
					value = obj.getValue();
				
				String s=" ";
				if(key.equalsIgnoreCase("authorization")) {
					s+=getAuthorization(token,obj.getValueList(),credentials,message);
				}
				else {
					for(objects x:obj.getValueList())
					{
						if(x.getKey().indexOf("timestamp")!=-1) {
							credentials.put(x.getKey(), timestamp());
						}
						if(x.getKey().indexOf("signature")!=-1) {
							
							credentials.put(x.getKey(),signature(token,credentials,message));
						}
						if(x.getValue().equals("codeValue"))
							s+= x.getKey() + "=" + encode(credentials.get(x.getKey()))+",";
						else if(x.getValue().equals("codeValueSpace")) {
							System.out.println(credentials.keySet()+" :: "+credentials.values());
							System.out.println(x.getKey() + "\n" + credentials.get(x.getKey()));
							s+= encode(credentials.get(x.getKey()))+",";
						}
						else
							s+= x.getKey() + "=" + encode(x.getValue())+",";
					}
				}
				value+= s.substring(0,s.length()-1);
				System.out.println(message+" "+key+" : "+value);
				headers.add(key, value);
			}
		headers.add("Cache-Control", "no-cache");
        headers.add("Access-Control-Allow-Origin", "*");
		return headers;
	}
	protected MultiValueMap<String,String> buildBody(UrlObject token, Map<String, String> credentials,String message)
	{
		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		System.out.println(message+" "+"::BODY::");
		for(objects obj:token.getResponseBody())
		{
			String key,value;
			key = obj.getKey();
			
			if(obj.getValue().equals("codeValue"))
				value = credentials.get(key);
			else
				value = obj.getValue();
			
			String s=" ";
			for(objects x:obj.getValueList())
			{
				if(x.getValue().equals("codeValue"))
					s+= x.getKey() + "=" + credentials.get(x.getKey())+",";
				else if(x.getValue().equals("codeValueSpace"))
					s+= credentials.get(x.getKey())+",";
				else
					s+= x.getKey() + "=" + x.getValue()+",";
			}
			
			value+= s.substring(0,s.length()-1);
			System.out.println(message+" "+key+" : "+value);
			body.add(key, value);
		}		
		return body;
	}
	
}
