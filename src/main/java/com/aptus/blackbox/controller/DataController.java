package com.aptus.blackbox.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aptus.blackbox.Service.Credentials;

@RestController
public class DataController {
	
	private String mongoUrl;
	
	public DataController(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	@Autowired
	private Credentials credentials;
	
	@RequestMapping(method = RequestMethod.GET, value = "/authdesination")
	private void destination(@RequestParam("data") Map<String,String> data) {
		try {
			HashMap<String,String> destCred = new HashMap<>();
			destCred.put("dbName", "mysql");
			destCred.put("userName", "mysql");
			destCred.put("password", "mysql");
			destCred.put("host", "mysql");
			destCred.put("port", "mysql");
			credentials.setDestToken(destCred);
			 
			
	    }
		catch(Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
}
