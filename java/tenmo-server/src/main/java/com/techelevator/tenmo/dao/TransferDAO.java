package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDAO 
{
	/**
	 * May not be implemented
	 * @param id
	 * @return
	 */
	List<Transfer> findAllByUser(int id);
	
	List<Transfer> findPendingByUser(int id);
	
	Transfer findByTransferId(int id);
	
	void sendMoney(int toId, int fromId, BigDecimal amount, boolean isValid);
	
	void requestMoney(int toId, int fromId, BigDecimal amount);
	
	boolean updateStatus(int id, String status);
	
	boolean approveRequest(int id, int userId);
	
	boolean denyRequest(int id, int userId);
	
}
