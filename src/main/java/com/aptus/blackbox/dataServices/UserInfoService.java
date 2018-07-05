package com.aptus.blackbox.dataServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataRepositories.UserInfoImpl;
import com.aptus.blackbox.datamodels.UserInfo;
import com.mongodb.WriteResult;


@Service
public class UserInfoService {


	@Autowired
	private  UserInfoImpl userInfoImpl;

	public List<UserInfo> getAllUsers(){
		return userInfoImpl.getAllUsers();
	}

	public boolean userExist(String email) {
		return userInfoImpl.getUserByEmail(email)!=null;
	}
	
	public UserInfo getUserByEmail(String email) {
		return userInfoImpl.getUserByEmail(email);
	}
	
	public UserInfo createUser(UserInfo userInfo) {
		return userInfoImpl.createUser(userInfo);
	}


	public boolean deleteUser(String email) {
		return userInfoImpl.deleteUser(email);
	}

	
	public UserInfo updateUser(String email, String key, String value) {
		if(userInfoImpl.getUserByEmail(email)==null)
			return null;
	    return userInfoImpl.updateUser(email, key, value);
	}

}
