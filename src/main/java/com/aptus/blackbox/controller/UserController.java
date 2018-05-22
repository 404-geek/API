package com.aptus.blackbox.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aptus.blackbox.models.User;
import com.aptus.blackbox.service.UserService;
import com.google.gson.JsonObject;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	@RequestMapping("/getUserDetails")
	public ResponseEntity<String> getUsers(@RequestParam("email") String email) {
		JsonObject obj =new JsonObject();
		User user = userService.getUserByEmail(email);
		if(!userService.userExistsByEmail(email)) {
			obj.addProperty("Result", "User not found");
			return ResponseEntity.status(HttpStatus.OK).headers(null).body(obj.toString());
		}
		obj.addProperty("FirstName", user.getFirstName());
		obj.addProperty("LastName", user.getLastName());
		obj.addProperty("Email", user.getEmail());
		obj.addProperty("Contact", user.getContact());
		obj.addProperty("Company", user.getCompany());
		obj.addProperty("Role", user.getRole());
		
		return ResponseEntity.status(HttpStatus.OK).headers(null).body(obj.toString());
		
	}
	
	@RequestMapping("/addUser")
	public ResponseEntity<String> addUser(@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam("company") String company,
			@RequestParam("email") String email,
			@RequestParam("contact") String contact,
			@RequestParam("password") String password) {
		
		JsonObject obj=new JsonObject();
		
		if(userService.userExistsByEmail(email)) {
			obj.addProperty("Result", "User email already registered");
			return ResponseEntity.status(HttpStatus.OK).headers(null).body(obj.toString());
		}
		User user=new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setCompany(company);
		user.setEmail(email);
		user.setContact(contact);
		user.setPassword(password);
		
		userService.addUser(user);
		obj.addProperty("Result", "User Registration successfull");
		return ResponseEntity.status(HttpStatus.OK).body(obj.toString());
		
	}
}
