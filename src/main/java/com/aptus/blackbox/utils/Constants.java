package com.aptus.blackbox.utils;

public final class Constants {

	private Constants(){}
	
	public final static int USER_NOT_FOUND_CODE = 404;
	public final static String USER_NOT_FOUND_MSG = "User Not Found";
	
	public final static int USER_EXIST_CODE = 201;
	public final static String USER_EXIST_MSG = "User Already Exist";
	
	public final static int SUCCESS_CODE = 200;
	public final static String SUCCESS_MSG = "Success";
	
	public final static int FAILED_CODE = 400;
	public final static String FAILED_MSG = "Failed";
	
	public final static int INVALID_CREDENTIALS_CODE = 202;
	public final static String INVALID_CREDENTIALS_MSG = "Invalid Credentials";
	
	public final static int INTERNAL_SERVER_ERROR = 500;
	
	
	public final static String RESPONSE_CODE = "status";
	public final static String RESPONSE_MESSAGE = "message";
	public final static String RESPONSE_DATA = "data";
	
	public final static int SOURCE_INVALID = 450;
	public final static int DESTINATION_INVALID = 451;
	public final static int SRC_DEST_INVALID = 452;
	

	
	//Collection Names
	public final static String COLLECTION_USERINFO = "userInfo";
	public final static String COLLECTION_SOURCECREDENTIALS = "sourceCredentials";
	public final static String COLLECTION_DESTINATIONCREDENTIALS = "destinationCredentials";
	
	//Collection Primary Key
	public final static String SRCDESTCRED_ID="_id";
	
}
