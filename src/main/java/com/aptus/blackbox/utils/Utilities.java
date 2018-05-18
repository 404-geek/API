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
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.models.UrlObject;
import com.aptus.blackbox.models.objects;
import com.google.gson.JsonObject;

import sun.misc.*;

public class Utilities {
	public static boolean isSessionValid(HttpSession session,Credentials credentials)
	{
		return session.getId().equals(credentials.getSessionId().get(credentials.getUserId()));		
	}	
	public static void valid()
	{
		System.out.println("********************Valid******************");
	}
	public static void invalid()
	{
		System.out.println("*****************Invalid***************");
	}
	public static boolean postpatchMetaData(JsonObject body, String type, String method,String userId,String mongoUrl) {
		try {

			System.out.println("postpatchMetaData:\nBody: "+body.toString()+"\nType: "+type+"\nMethod"+method);
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
