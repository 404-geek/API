package com.aptus.blackbox.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.aptus.blackbox.dataService.Credentials;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	public static List<String> check(String key, JsonElement jsonElement,List<String> list) {
        if (jsonElement.isJsonArray()) {
            for (JsonElement jsonElement1 : jsonElement.getAsJsonArray()) {
                list=check(key, jsonElement1,list);
            }
        } else {
            if (jsonElement.isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> entrySet = jsonElement
                        .getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    String key1 = entry.getKey();
                    if (key1.equals(key)) {
                        list.add(entry.getValue().getAsString());
                    }
                    list=check(key, entry.getValue(),list);
                }
            } else {
                if (jsonElement.toString().equals(key)) {
                    list.add(jsonElement.getAsString());
                }
            }
        }
        return list;
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
