package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountSqlDAO implements AccountDAO 
{
	private JdbcTemplate jdbcTemplate;

    public AccountSqlDAO(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
	@Override
	public BigDecimal getBalance(int id) {
		String cmd = "SELECT balance FROM accounts WHERE user_id = ?;";
		return jdbcTemplate.queryForObject(cmd, BigDecimal.class, id);
	}
	@Override 
	public boolean addToBalance(int id, BigDecimal moneyToAdd) {
		String cmd = "UPDATE accounts SET balance = ? WHERE user_id = ?";
		return jdbcTemplate.update(cmd, getBalance(id).add(moneyToAdd), id) == 1;
	}
}
