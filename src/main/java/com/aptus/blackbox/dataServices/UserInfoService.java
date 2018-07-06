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

	public boolean userExist(String _id) {
		return userInfoImpl.getUserById(_id)!=null;
	}
	
	public boolean userValid(String _id,String password) {
		return userInfoImpl.matchSingleField(_id, "userPassword", password);
	}
	
	public UserInfo getUserByEmail(String _id) {
		return userInfoImpl.getUserById(_id);
	}
	
	//public boolean isValidUser
	
	public UserInfo createUser(UserInfo userInfo) {
		return userInfoImpl.createUser(userInfo);
	}


	public boolean deleteUser(String email) {
		return userInfoImpl.deleteUser(email);
	}

	
	public UserInfo updateUser(String _id, String key, String value) {
		if(userInfoImpl.getUserById(_id)==null)
			return null;
	    return userInfoImpl.updateUser(_id, key, value);
	}

}
