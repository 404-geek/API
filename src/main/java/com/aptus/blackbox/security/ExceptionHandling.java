package com.aptus.blackbox.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.gson.JsonObject;

public class ExceptionHandling {
	
	public ResponseEntity<String> clientException(HttpStatusCodeException exception)
	{
		 JsonObject respBody = new JsonObject();
		
		 switch (((HttpStatusCodeException) exception).getStatusCode()) {
		
		 case BAD_REQUEST:
		 {

			 respBody.addProperty("code", "400");
			 respBody.addProperty("message", "Bad Request");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 
		 case UNAUTHORIZED:
		 {

			 respBody.addProperty("code", "401");
			 respBody.addProperty("message", "UNAUTHORIZED");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 
		 case NOT_FOUND:
		 { 

			 respBody.addProperty("code", "404");
			 respBody.addProperty("message", "NOT FOUND");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 case REQUEST_TIMEOUT:
		 {

			 respBody.addProperty("code", "408");
			 respBody.addProperty("message", "REQUEST TIMEOUT");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());  
		 }
		 
		 case CONFLICT:{

			 respBody.addProperty("code", "409");
			 respBody.addProperty("message", "Bad Gateway");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 case GONE:{

			 respBody.addProperty("code", "410");
			 respBody.addProperty("message", "CONFLICT");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 case TOO_MANY_REQUESTS:{

			 respBody.addProperty("code", "429");
			 respBody.addProperty("message", "TOO MANY REQUESTS");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 
		 case INTERNAL_SERVER_ERROR:
		 {
			 
			 respBody.addProperty("code", "500");
			 respBody.addProperty("message", "Internal Server Error");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
			 
		 }
		 case BAD_GATEWAY:
		 {
			 respBody.addProperty("code", "502");
			 respBody.addProperty("message", "Bad Gateway");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString()); 
		 }
		 case GATEWAY_TIMEOUT:
		 {
			 respBody.addProperty("code", "504");
			 respBody.addProperty("message", "Geteway Timeout");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
		 }
		 case SERVICE_UNAVAILABLE:
		 {
			 respBody.addProperty("code", "503");
			 respBody.addProperty("message", "Service Unavailable");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
		 }
		 
		 default:
		 {
			 respBody.addProperty("code", "4xx/5xx");
			 respBody.addProperty("message", "Client/Server Error");
			 System.out.println(respBody.toString());
			 return ResponseEntity.status(HttpStatus.OK).body(respBody.toString());
		 }
		 
		 
		 }
		
		
		
	}
	
	
	
	
	
	 
	 
	
	
}

	

