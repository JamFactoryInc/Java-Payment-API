package com.techelevator.tenmo.models;

import java.math.BigDecimal;

public class Account {
	private int id;

	private int userId;
	
	private BigDecimal balance;
	
	public Account()
	{
		
	}
	
	public Account(int id, int userId, BigDecimal balance)
	{
		this.id = id;
		this.userId = userId;
		this.balance = balance;
	}
	
	public int getId() 
	{
		return id;
	}

	public int getUserId() 
	{
		return userId;
	}

	public BigDecimal getBalance() 
	{
		return balance;
	}
}
