package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.BasicUser;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDAO {

    List<User> findAll();
    //Seriously? Do you *want* us to send user password hashes out to anyone who asks?
    List<BasicUser> findAllUnauthenticated();

    User findByUsername(String username);

    int findIdByUsername(String username);
    String findUsernameByAccountId(int id);
    
    public User findByUserId(int id);

    boolean create(String username, String password);
}
