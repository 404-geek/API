package com.aptus.blackbox.dataServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.UserInfoDAO;
import com.aptus.blackbox.dataInterfaces.VerificationTokenDAO;
import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.datamodels.UserInfo;
import com.aptus.blackbox.datamodels.VerificationToken;
import com.aptus.blackbox.index.ScheduleInfo;


@Service
public class UserService {


	@Autowired
	private UserInfoDAO userInfoDAO;
	
	@Autowired
	private VerificationTokenDAO verificationTokenDAO;
	
	@Autowired
	private ApplicationCredentials applicationCredentials;
	
	@Autowired
	private UserConnectorService  userConnectorService;
	
	@Autowired
	private MeteringService meteringService;
	
	@Autowired
	private SchedulingService schedulingService;
	
	
	
	
	
	
	
	
	
	

	public List<UserInfo> getAllUsers(){
		return userInfoDAO.getAllUsers();
	}

	public UserInfo userExist(String email) {
		return userInfoDAO.getUserByEmail(email);
	}
	

	
	public boolean userValid(String _id,String password) {
		return userInfoDAO.matchSingleField(_id, "userPassword", password);
	}
	
	public UserInfo getUserByEmail(String _id) {
		return userInfoDAO.getUserById(_id);
	}
	
	
	
	public UserInfo createUser(UserInfo userInfo) {
		return userInfoDAO.createUser(userInfo);
	}


	public boolean deleteUser(String email) {
		return userInfoDAO.deleteUser(email);
	}

	
	public UserInfo updateUser(String _id, String key, Object value) {
		if(userInfoDAO.getUserById(_id)==null)
			return null;
	    return userInfoDAO.updateUser(_id, key, value);
	}
	
	public void createVerificationToken(String userId, String token) {
		verificationTokenDAO.createVerificationToken(userId, token);
	}

	public VerificationToken getVerificationToken(String token) {
		return verificationTokenDAO.getVerificationToken(token);
	}
	
	/*
	 * Initializing meta-data tables on email confirmation
	 * Creating object in applicationCred for User
	 */
	public void registerUser(String token,String userId) {
		
		applicationCredentials.setApplicationCred(userId, new ScheduleInfo());
		userConnectorService.createUser(userId);
		meteringService.createUser(userId);
		schedulingService.createUser(userId);
		
		// invalidate token
		verificationTokenDAO.invalidateToken(token);
		
		// validate user
		updateUser(userId, "isEnabled", true);
		
		
		
	}
}
