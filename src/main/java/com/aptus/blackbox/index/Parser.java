package com.aptus.blackbox.index;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.gson.Gson;

public class Parser {
	
	private SrcObject srcProp;
	private DestObject destProp;
	public Parser(String type)
	{
		try {
		Gson gson=new Gson();
		String path="/home/sourav/Documents/sts space/BlackBoxReloaded/src/main/resources/static/json_config/";
		if(type.toLowerCase().startsWith("source")){
			path+=type+".json";
			BufferedReader br=new BufferedReader(new FileReader(path));
			srcProp = gson.fromJson(br, SrcObject.class);
		}
		else if(type.toLowerCase().startsWith("destination")){
			path+=type+".json";
			BufferedReader br=new BufferedReader(new FileReader(path));
			destProp = gson.fromJson(br, DestObject.class);
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
