package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.UserInfo;
import com.aptus.blackbox.datamodels.VerificationToken;

public interface VerificationTokenDAO {
	void createVerificationToken(String userId, String token);
 	VerificationToken getVerificationToken(String token);
 	boolean invalidateToken(String token);
}
