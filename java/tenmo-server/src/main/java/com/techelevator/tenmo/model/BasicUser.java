package com.techelevator.tenmo.model;

//This class corresponds to the 'User' class in the client, while the 'User' class here corresponds to the 'AuthenticatedUser' class in the client.
public class BasicUser {
	private String username;
	private Integer id;
	
	public String getUsername(){return username;}
	public Integer getId(){return id;}
	
	public BasicUser(String username, Integer id){
		this.username = username;
		this.id = id;
	}
}
