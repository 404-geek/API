package com.aptus.blackbox.index;

import java.io.Serializable;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.models.DestObject;
import com.aptus.blackbox.models.SrcObject;
import com.aptus.blackbox.security.ExceptionHandling;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Parser implements Serializable {
	Parser() {
	}

	private SrcObject srcProp;
	private DestObject destProp;

	public Parser(String type, String Id, String mongoUrl) {

		try {

			ResponseEntity<String> ret = null;
			ret = parsingJson(type, Id, mongoUrl);

			if (new Gson().fromJson(ret.getBody(), JsonObject.class).getAsJsonObject().get("code").getAsString()
					.equals("200")) {
				System.out.println("successful");
			}
			
			System.out.println("unsuccessful connection");
		}

		catch (Exception e) {
			System.out.println("inside parser exception");
			System.out.println(e);
		}
	}

	public ResponseEntity<String> parsingJson(String type, String Id, String mongoUrl) {

		JsonObject respBody = new JsonObject();
		ResponseEntity<String> out = null;

		try {
			Gson gson = new Gson();
			RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			String url = mongoUrl + "/credentials/" + type + "/" + Id.toUpperCase();
			System.out.println("inside parser function");
			System.out.println(url);
			out = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
			respBody.addProperty("message", "success");
			respBody.addProperty("status", "200");
			System.out.println(out.getBody());

			if (type.equalsIgnoreCase("source")) {
				srcProp = gson.fromJson(out.getBody(), SrcObject.class);
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());

			} else if (type.equalsIgnoreCase("destination")) {
				destProp = gson.fromJson(out.getBody(), DestObject.class);
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody.toString());
		}

		catch (HttpStatusCodeException e) {

			System.out.println("Inside token catch");
			e.getStatusCode();

			ExceptionHandling exceptionhandling = new ExceptionHandling();
			out = exceptionhandling.clientException(e);
			return out;
		}

		catch (Exception e) {
			System.out.println(e);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody.toString());
	}

	public SrcObject getSrcProp() {
		return this.srcProp;
	}

	public DestObject getDestProp() {
		return this.destProp;
	}
}
