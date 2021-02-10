package com.techelevator.tenmo.models;

import java.math.BigDecimal;

public class Transfer 
{
	private int id;
	private String type;
	private String status;
	private int accountTo;
	private String userTo;
	private int accountFrom;
	private String userFrom;
	private BigDecimal amount;
	
	public Transfer(){}
	public int getId(){return id;}
	public String getType(){return type;}
	public String getStatus(){return status;}
	public int getAccountTo(){return accountTo;}
	public int getAccountFrom(){return accountFrom;}
	public String getUserTo(){return userTo;}
	public String getUserFrom(){return userFrom;}
	public BigDecimal getAmount(){return amount;}
	public void setId(int id){this.id = id;}
	public void setType(String type){this.type = type;}
	public void setStatus(String status){this.status = status;}
	public void setAccountTo(int accountTo){this.accountTo = accountTo;}
	public void setAccountFrom(int accountFrom){this.accountFrom = accountFrom;}
	public void setUserTo(String userTo){this.userTo = userTo;}
	public void setUserFrom(String userFrom){this.userFrom = userFrom;}
	public void setAmount(BigDecimal amount){this.amount = amount;}
	
}
