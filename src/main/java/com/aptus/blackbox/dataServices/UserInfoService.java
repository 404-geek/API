package com.aptus.blackbox.dataServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.UserInfoDAO;
import com.aptus.blackbox.datamodels.UserInfo;


@Service
public class UserInfoService {


	@Autowired
	private UserInfoDAO userInfoDAO;

	public List<UserInfo> getAllUsers(){
		return userInfoDAO.getAllUsers();
	}

	public boolean userExist(String _id) {
		return userInfoDAO.getUserById(_id)!=null;
	}
	
	public boolean userValid(String _id,String password) {
		return userInfoDAO.matchSingleField(_id, "userPassword", password);
	}
	
	public UserInfo getUserByEmail(String _id) {
		return userInfoDAO.getUserById(_id);
	}
	
	//public boolean isValidUser
	
	public UserInfo createUser(UserInfo userInfo) {
		return userInfoDAO.createUser(userInfo);
	}


	public boolean deleteUser(String email) {
		return userInfoDAO.deleteUser(email);
	}

	
	public UserInfo updateUser(String _id, String key, String value) {
		if(userInfoDAO.getUserById(_id)==null)
			return null;
	    return userInfoDAO.updateUser(_id, key, value);
	}

}
