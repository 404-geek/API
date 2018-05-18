package com.aptus.blackbox.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, String>{
	//public User findByEmail(String email);
}
