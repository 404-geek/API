package com.aptus.blackbox.datamodels;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class VerificationToken implements Serializable{
	

	private static final int EXPIRATION = 60 * 24;

	@Id
	private String id;
	
	private String token;

	private String email;

	private Date expiryDate;
	
	private boolean isValid;

	public VerificationToken(String token, String email) {
		//this.id = UUID.randomUUID().toString();
		this.token = token;
		this.email = email;
		this.setValid(true);
		this.expiryDate = calculateExpiryDate(EXPIRATION);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getEmail() {
		return email;
	}

	public void setUserId(String email) {
		this.email = email;
	}
	


	private Date calculateExpiryDate(int expiryTimeInMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Timestamp(cal.getTime().getTime()));
		cal.add(Calendar.MINUTE, expiryTimeInMinutes);
		return new Date(cal.getTime().getTime());
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	

}
