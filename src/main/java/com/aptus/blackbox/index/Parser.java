package com.aptus.blackbox.index;

import java.io.Serializable;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.aptus.blackbox.datamodels.DestinationConfig;
import com.aptus.blackbox.datamodels.SourceConfig;
import com.aptus.blackbox.security.ExceptionHandling;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
@Component
public class Parser implements Serializable {
	Parser() {
	}

	private SourceConfig srcProp;
	private DestinationConfig destProp;


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
				srcProp = gson.fromJson(out.getBody(), SourceConfig.class);
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());

			} else if (type.equalsIgnoreCase("destination")) {
				destProp = gson.fromJson(out.getBody(), DestinationConfig.class);
				return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			}

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody.toString());
		}

		catch (HttpStatusCodeException e) {

			System.out.println("Inside token catch");
			e.printStackTrace();
			
//			ExceptionHandling exceptionhandling = new ExceptionHandling();
//			out = exceptionhandling.clientException(e);
		}

		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody.toString());
	}

	public SourceConfig getSrcProp() {
		return this.srcProp;
	}

	public DestinationConfig getDestProp() {
		return this.destProp;
	}
}
