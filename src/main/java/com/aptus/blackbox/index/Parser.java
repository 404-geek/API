package com.aptus.blackbox.index;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.gson.Gson;

public class Parser {
	
	private JsonProp jsonprop;
	public Parser(String type)
	{
		try {
		Gson gson=new Gson();
		String path="/home/sourav/Documents/sts space/BlackBoxReloaded/src/main/resources/static/json_config/";
		path+=type+".json";
		BufferedReader br=new BufferedReader(new FileReader(path));
		jsonprop = gson.fromJson(br, JsonProp.class);
		
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	public JsonProp getProp()
	{
		return this.jsonprop;
	}
}
