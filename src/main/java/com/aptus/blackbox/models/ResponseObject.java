package com.aptus.blackbox.models;

import com.aptus.blackbox.utils.Constants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseObject {

	JsonObject obj;
	
	public ResponseObject() {
		obj = new JsonObject();
	}
	public JsonObject Response(int code,String message, String data) {
		this.obj.addProperty(Constants.RESPONSE_CODE,code );
		this.obj.addProperty(Constants.RESPONSE_MESSAGE, message);
		
		if(data.length()!=0)
		this.obj.addProperty(Constants.RESPONSE_DATA, data);
		return this.obj;
		
	}
	
	public JsonObject Response(int code,String message, Boolean data) {
		this.obj.addProperty(Constants.RESPONSE_CODE,code );
		this.obj.addProperty(Constants.RESPONSE_MESSAGE, message);
		this.obj.addProperty(Constants.RESPONSE_DATA, data);
		return this.obj;
		
	}
	
	public JsonObject Response(int code,String message, Character data) {
		this.obj.addProperty(Constants.RESPONSE_CODE,code );
		this.obj.addProperty(Constants.RESPONSE_MESSAGE, message);
		this.obj.addProperty(Constants.RESPONSE_DATA, data);
		return this.obj;
		
	}
	
	public JsonObject Response(int code,String message, Number data) {
		this.obj.addProperty(Constants.RESPONSE_CODE,code );
		this.obj.addProperty(Constants.RESPONSE_MESSAGE, message);
		this.obj.addProperty(Constants.RESPONSE_DATA, data);
		return this.obj;
		
	}
	
	public JsonObject Response(int code,String message, JsonElement data) {
		this.obj.addProperty(Constants.RESPONSE_CODE,code );
		this.obj.addProperty(Constants.RESPONSE_MESSAGE, message);
		this.obj.add(Constants.RESPONSE_DATA, data);
		return this.obj;
		
	}
	
	
}
