package com.aptus.blackbox.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sun.misc.BASE64Encoder;

public class Utilities {public static boolean isSessionValid(HttpSession session,ApplicationCredentials credentials,String UserID)
{
//	
/*	System.out.println("==========SESSION===BEGIN========");
	System.out.println("SESSION\t"+session.getId().equals(credentials.getSessionId().get(UserID)));
	System.out.println("SESSION ID\t"+session.getId());
	System.out.println("SESSION USER\t"+UserID);
	System.out.println("SESSION CRED SESSION ID\t"+credentials.getSessionId().get(UserID));

	System.out.println("==========SESSION===END=======");
*/

	return session.getId().equals(credentials.getSessionId().get(UserID));		
}	
private static String timestamp() {
	String time = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
	return time.substring(0, time.length()-3);
}
private static String signature(UrlObject object,Map<String,String> params, Map<String, String> values,String message) throws Exception {
	String consumerSecret=null;
	for(objects obj:object.getSignature()){
		if(obj.getKey().toLowerCase().indexOf("secret")!=-1) {
			consumerSecret = obj.getValue().toString();
		}
	}
	String baseSignature = object.getMethod().toString()+"&"+
	encode(object.getUrl())+"&";
	String parameter="",value="";
	if(object.getParams()!=null) {
		for(objects ob:object.getParams()) {
			if(ob.getValue().equalsIgnoreCase("codevalue")) {
				value=values.get(ob.getKey());
			}
			else {
				value = ob.getValue();
			}
			params.put(encode(ob.getKey()), encode(value));
		}
	}
	if(object.getResponseBody()!=null) {
		for(objects ob:object.getResponseBody()) {
			params.put(encode(ob.getKey()), encode(ob.getValue()));
		}
	}
	for(String key:params.keySet()) {
		parameter+=key+"="+params.get(key)+"&";
	}
	parameter = parameter.substring(0, parameter.length()-1);
	parameter = baseSignature+encode(parameter);
	consumerSecret=encode(consumerSecret)+"&";
	if((object.getLabel().toLowerCase().indexOf("token")!=-1)&&(values.containsKey("oauth_token")))
		consumerSecret+=values.get("oauth_token");
	System.out.println(message+" "+parameter+"\n"+consumerSecret);
	return generateSignature(parameter,consumerSecret);
}
private static String encode(String value) {
	String encoded = "";
	try {
		encoded = URLEncoder.encode(value, "UTF-8");
	} catch (Exception e) {
		e.printStackTrace();
	}
	String sb = "";
	char focus;
	for (int i = 0; i < encoded.length(); i++) {
		focus = encoded.charAt(i);
		if (focus == '*') {
			sb += "%2A";
		} else if (focus == '+') {
			sb += "%20";
		} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
				&& encoded.charAt(i + 2) == 'E') {
			sb += '~';
			i += 2;
		} else {
			sb += focus;
		}
	}
	return sb.toString();
}
private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

public static byte[] calculateRFC2104HMAC(String data, String key)
	throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
{
	SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
	Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
	mac.init(signingKey);
	return mac.doFinal(data.getBytes());
}
public static String generateSignature(String data,String key) throws Exception {
	String hmac = new BASE64Encoder().encode(calculateRFC2104HMAC(data,key));
	return hmac;
}
public static String buildUrl(UrlObject token, Map<String, String> values,String message)
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
	String url= url(token.getUrl(), values)+params.substring(0,params.length()-1);
	System.out.println(message+" "+url);
	return url;
	
}
public static String url(String url, Map<String, String> values) {
String newUrl = "";
for (String comp : url.split("/")) {
	if (comp.startsWith("{")) {
		newUrl += (values.get(comp.substring(1, comp.length() - 1)) + "/");
	} else {
		newUrl += (comp + "/");
	}
}
return newUrl.substring(0, newUrl.length() - 1);
}
public static HttpHeaders buildHeader (UrlObject token, Map<String, String> values,String message) throws Exception
{
	Map<String, String> oauth; 
	System.out.println(message+" "+"::HEADERS::");
	System.out.println(values.keySet().toString());
	System.out.println(values.values().toString());
	HttpHeaders headers = new HttpHeaders();
	for(objects obj:token.getHeader())
		{
			String key,value;
			key = obj.getKey();
			
			if(obj.getValue().equals("codeValue"))
				value = values.get(key);
			else
				value = obj.getValue();
			
			String s=" ";
			oauth = new TreeMap<String, String>();
			for(objects x:obj.getValueList())
			{
				if(x.getKey().indexOf("timestamp")!=-1) {
					values.put(x.getKey(), timestamp());
				}
				if(x.getKey().indexOf("signature")!=-1) {
					
					values.put(x.getKey(),signature(token,oauth,values,message));
				}
				if(x.getValue().equals("codeValue"))
					s+= x.getKey() + "=" + encode(values.get(x.getKey()))+",";
				else if(x.getValue().equals("codeValueSpace")) {
					System.out.println(values.keySet()+" :: "+values.values());
					System.out.println(x.getKey() + "\n" + values.get(x.getKey()));
					s+= encode(values.get(x.getKey()))+",";
				}
				else
					s+= x.getKey() + "=" + encode(x.getValue())+",";
				if(value.equalsIgnoreCase("oauth")) {
					if(x.getValue().equals("codeValue"))
						oauth.put(encode(x.getKey()),encode(values.get(x.getKey())));
					else
						oauth.put(encode(x.getKey()),encode(x.getValue()));
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
public static MultiValueMap<String,String> buildBody(UrlObject token, Map<String, String> values,String message)
{
	MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
	System.out.println(message+" "+"::BODY::");
	for(objects obj:token.getResponseBody())
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
		System.out.println(message+" "+key+" : "+value);
		body.add(key, value);
	}		
	return body;
}

public static ResponseEntity<String> token(UrlObject object,Map<String, String> values,String message) {
	ResponseEntity<String> out = null;
	try {
		RestTemplate restTemplate = new RestTemplate();

		System.out.println("Token called from "+message);
		System.out.println("***************Build Url Started***************");
		String url = Utilities.buildUrl(object, values,message);
		System.out.println(object.getLabel()+" Url:"+url);		
		System.out.println("***************Build Url Stopped***************");
		
		

		System.out.println("***************Build Header Started***************");
		HttpHeaders headers = Utilities.buildHeader(object, values,message);
		System.out.println("***************Build Header Stopped***************");
		
		System.out.println("***************Build Body Started***************");
		HttpEntity<?> httpEntity;
		if (!object.getResponseBody().isEmpty()) {
			MultiValueMap<String, String> preBody = Utilities.buildBody(object, values,"");
			Object postBody=null;
			for(objects head:object.getHeader())
			{
				if(head.getKey().equalsIgnoreCase("content-type")) {
					postBody=Utilities.bodyBuilder(head.getValue(),preBody,message);
					break;
				}
			}
			httpEntity = new HttpEntity<Object>(postBody, headers);
		} else {
			httpEntity = new HttpEntity<Object>(headers);
		}
		System.out.println("***************Build Body Stopped***************");
//		if (object.getResponseString()!=null&&!object.getResponseString().isEmpty()) {
//			httpEntity = new HttpEntity<Object>(object.getResponseString(), headers);
//		} else if (!object.getResponseBody().isEmpty()) {
//			MultiValueMap<String, String> body = Utilities.buildBody(object, values,message);
//			httpEntity = new HttpEntity<Object>(body, headers);
//		} else {
//			httpEntity = new HttpEntity<Object>(headers);
//		}
		HttpMethod method = (object.getMethod().equals("GET")) ? HttpMethod.GET : HttpMethod.POST;
		System.out.println(message+" "+"Method : "+method);
		URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
		System.out.println(message+" "+"----------------------------"+uri);
		out = restTemplate.exchange(URI.create(url), method, httpEntity, String.class);
		//System.out.println(out.getBody());
	} 
	catch(ResourceAccessException e) {
		
		out =  new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
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
public static void valid()
{
	System.out.println("********************Valid******************");
}
public static void invalid()
{
	System.out.println("*****************Invalid***************");
}
public static Object bodyBuilder(String contentType, MultiValueMap<String, String> preBody,String message) throws Exception {
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
private static String HtmlBuilder(MultiValueMap<String, String> preBody, String message) {
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
private static String XmlBuilder(MultiValueMap<String, String> preBody, String message) {
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
private static String JsonBuilder(MultiValueMap<String, String> preBody, String message) {
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
	
	public static List<String> checkByPath(String path[],final int pos, JsonElement jsonElement, List<String> list) {

		if (jsonElement.isJsonObject()) {

//			System.out.println("All keys");
//			jsonElement.getAsJsonObject().entrySet().forEach(e -> System.out.println(e.getKey()));

			Entry<String, JsonElement> entry = jsonElement.getAsJsonObject().entrySet().stream()
					.filter(e -> e.getKey().equals(path[pos])).findFirst().orElse(null);
	//		System.out.println("k=" + entry.getKey());
			if (entry == null)
				return list;
				

			if (pos == path.length - 1) {
				list.add(entry.getValue().getAsString());
				return list;
			}
			return checkByPath(path, pos + 1, entry.getValue(), list);

		} else if (jsonElement.isJsonArray()) {
			for (JsonElement jsonElement1 : jsonElement.getAsJsonArray()) {
				list = checkByPath(path, pos , jsonElement1, list);
			}
		}
		return list;
	}

	public static boolean postpatchMetaData(JsonObject body, String type, String method,String userId,String mongoUrl) {
		try {

			
			ResponseEntity<String> out = null;
			String url = "";
			String appname = "";
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			HttpMethod met = null;
			String filter = "";
			if (method.equalsIgnoreCase("POST")) {
				url = mongoUrl + "/credentials/" + type.toLowerCase() + "Credentials";
				met = HttpMethod.POST;
			} else if (method.equalsIgnoreCase("PATCH")) {
				url = mongoUrl + "/credentials/" + type.toLowerCase() + "Credentials/"
						+ userId.toLowerCase();
				met = HttpMethod.PATCH;
			} 
			System.out.println(url + "\n" + met);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", "application/json");
			headers.add("Cache-Control", "no-cache");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(body.toString(), headers);
			out = restTemplate.exchange(url, met, httpEntity, String.class, filter);
			if (out.getStatusCode().is2xxSuccessful()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("source.postpatchmetadata");
		}
		return false;
	}
	
}
