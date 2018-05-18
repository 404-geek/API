package com.aptus.blackbox.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.models.User;
import com.aptus.blackbox.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	List<User> users;
	
	public boolean userExistsByEmail(String email) {
		return userRepository.exists(email);
	}
	
	public User getUserByEmail(String email) {
		return  userRepository.findOne(email);
	}
	
	public void addUser(User user) {
		System.out.println("User "+userRepository.save(user));
	}
	
	public void updateUser(User user) {
		userRepository.save(user);
	}
	
	public void deleteUser(String email) {
		userRepository.delete(email);
	}
}
