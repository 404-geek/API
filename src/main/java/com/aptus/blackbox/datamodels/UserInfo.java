package com.aptus.blackbox.datamodels;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.JsonObject;
import javax.validation.constraints.NotNull;

@Document
public class UserInfo {

	
	@Id
//	private String userId;
//	@Indexed(unique=true)

	private String userId;
	private String userName;
	private String userPassword;
	private Date creationDate = new Date();
	
	


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getUserPassword() {
		return userPassword;
	}


	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}


	public Date getCreationDate() {
		return creationDate;
	}


	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}


	
	
	@Override
	public String toString() {
		JsonObject jobj  = new JsonObject();
		jobj.addProperty("email",this.userId);
		jobj.addProperty("username", this.userName);
		jobj.addProperty("creation Date", this.creationDate+"");
		return jobj.toString();
		
		}

	
	
}
