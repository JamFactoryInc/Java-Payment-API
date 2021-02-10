package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDAO 
{
	BigDecimal getBalance(int id);
	
	boolean addToBalance(int id, BigDecimal moneyToAdd);
}
