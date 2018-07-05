package com.aptus.blackbox.dataInterfaces;

import java.util.List;

import com.aptus.blackbox.datamodels.UserInfo;


public interface UserInfoDAO {
 
	UserInfo getUserByEmail(String email);
	List<UserInfo> getAllUsers();
	UserInfo createUser(UserInfo userInfo);
	boolean deleteUser(String email);
	UserInfo updateUser(String email, String field, String value);
	
}
