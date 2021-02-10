package com.techelevator.tenmo.model;

import java.math.BigDecimal;

import org.omg.CORBA.portable.IDLEntity;

public class Account 
{
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
