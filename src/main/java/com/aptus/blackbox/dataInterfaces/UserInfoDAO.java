package com.aptus.blackbox.dataInterfaces;

import java.util.List;
import java.util.Map;

import com.aptus.blackbox.datamodels.UserInfo;


public interface UserInfoDAO {
 
	UserInfo getUserById(String _id);
	List<UserInfo> getAllUsers();
	UserInfo createUser(UserInfo userInfo);
	boolean deleteUser(String email);
	UserInfo updateUser(String email, String field, String value);
	boolean matchSingleField(String _id, String key, String value);
	boolean matchMultipleField(String _id, Map<String, String> fields);
	
	
}
