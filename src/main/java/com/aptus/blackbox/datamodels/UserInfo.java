package com.aptus.blackbox.datamodels;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.JsonObject;

@Document
public class UserInfo {

	
	@Id
//	private String userId;
//	@Indexed(unique=true)
	private String email;
	private String username;
	private String password;
	private Date creationDate = new Date();
	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserName() {
		return username;
	}
	public void setUserName(String name) {
		this.username = name;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		JsonObject jobj  = new JsonObject();
		jobj.addProperty("email",this.email);
		jobj.addProperty("username", this.username);
		jobj.addProperty("creation Date", this.creationDate+"");
		return jobj.toString();
		
		}
	
	
}
