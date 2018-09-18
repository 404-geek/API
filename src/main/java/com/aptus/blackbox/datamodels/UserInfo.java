package com.aptus.blackbox.datamodels;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.JsonObject;
import javax.validation.constraints.NotNull;

@Document
public class UserInfo implements Serializable{

	
	@Id
	private String email;
//	private String userId;
//	@Indexed(unique=true)

	
	private String name;
	private String password;
	
	private String company;
	private String contact;
	
	private Date creationDate;
	private boolean isEnabled;

	
	
	
	
	
	
	
	
	public UserInfo() {
		contact = company = "";
		isEnabled = false;
		creationDate = new Date();
	}

	


	

	public Date getCreationDate() {
		return creationDate;
	}


	
	
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}






	public String getName() {
		return name;
	}






	public void setName(String name) {
		this.name = name;
	}






	public String getPassword() {
		return password;
	}






	public void setPassword(String password) {
		this.password = password;
	}






	public String getCompany() {
		return company;
	}






	public void setCompany(String company) {
		this.company = company;
	}






	public String getEmail() {
		return email;
	}






	public void setEmail(String email) {
		this.email = email;
	}






	public String getContact() {
		return contact;
	}






	public void setContact(String contact) {
		this.contact = contact;
	}






	public boolean isEnabled() {
		return isEnabled;
	}






	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}


	
	
/*	@Override
	public String toString() {
		JsonObject jobj  = new JsonObject();
		jobj.addProperty("email",this.userId);
		jobj.addProperty("username", this.userName);
		jobj.addProperty("creation Date", this.creationDate+"");
		return jobj.toString();
		
		}*/

	
}
