package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import com.techelevator.tenmo.model.Transfer;

import java.util.ArrayList;

@Service
public class TransferSqlDAO implements TransferDAO{

	private JdbcTemplate jdbcTemplate;
	
	private static final String APPROVE = "Approved";
	private static final String DENY = "Rejected";
	private static final String PENDING = "Pending";
	
	private static final String SEND = "Send";
	private static final String RECEIVE = "Request";
	
	//Stores valid status/type strings as found in the database.
	//Since database indices start at 1, a 0 indicates an invalid status/type.
	//Overkill for this project I know, but if you're bothering to have databases for
	//  these things you have to assume they're meant to be extensible.
	
	private List<String> validStatus;
	private List<String> validTypes;
	private int getStatusId(String status){
		for(int i = 1; i < validStatus.size(); i++){
			if(validStatus.get(i).toLowerCase().equals(status.toLowerCase())){return i;}
		}
		return 0;
	}
	private int getTypeId(String type){
		for(int i = 1; i < validTypes.size(); i++){
			if(validTypes.get(i).toLowerCase().equals(type.toLowerCase())){return i;}
		}
		return 0;
	}
	//Builds validStatus and validTypes lists from database;
	private void setupStatusType(){
		String cmd = "SELECT s.transfer_status_desc, t.transfer_type_desc FROM "
				   + "transfer_statuses as s FULL OUTER JOIN transfer_types as t ON "
				   + "s.transfer_status_id = t.transfer_type_id";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(cmd);
		
		validStatus = new ArrayList<String>();
		validTypes = new ArrayList<String>();
		validStatus.add("ERROR");
		validTypes.add("ERROR");
		
		while(rows.next()){
			String newStatus = rows.getString(1);
			String newType = rows.getString(2);
			if(newStatus != null){validStatus.add(newStatus);}
			if(newType != null){validTypes.add(newType);}
		}
	}
	private boolean validateStatusType(String[] status, String[] type){
		int result = 1;
		for(int i = 0; i < status.length; i++){
			result *= getStatusId(status[i]);
		}
		for(int i = 0; i < type.length; i++){
			result *= getTypeId(type[i]);
		}
		return result != 0;
	}
	
    public TransferSqlDAO(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
        setupStatusType();
        
        //TODO add validation that required status/types are present
        if(!validateStatusType(new String[]{PENDING, APPROVE, DENY},
        					   new String[]{RECEIVE, SEND})){
        	
        }
    }

	@Override
	public List<Transfer> findAllByUser(int id) {
		String cmd = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount "
				   + "FROM transfers as t INNER JOIN accounts as a ON a.account_id in (account_from, account_to) WHERE a.user_id = ?;";
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet(cmd, id);
		return mapRowsetToTransfer(rowSet);
	}
	
	@Override
	public List<Transfer> findPendingByUser(int id) {
		String cmd = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount "
				   + "FROM transfers as t INNER JOIN accounts as a ON a.account_id in (account_from, account_to) "
				   + "WHERE a.account_id = ? AND t.transfer_status_id = ?;";
		SqlRowSet rowSet = jdbcTemplate.queryForRowSet(cmd, id, getStatusId(PENDING));
		return mapRowsetToTransfer(rowSet);
	}

	@Override
	public Transfer findByTransferId(int id) {
		String cmd = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount "
				   + "FROM transfers WHERE transfer_id = ?";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(cmd, id);
		if(rows.next()){
			return mapRowToTransfer(rows);
		}
		return null;
	}

	@Override
	public void sendMoney(int toId, int fromId, BigDecimal amount, boolean isValid) {
		String cmd = "INSERT INTO transfers (transfer_type_id, transfer_status_id, amount, account_to, account_from) VALUES (?, ?, ?, "
				   + "(SELECT account_id FROM accounts WHERE user_id = ?), "
				   + "(SELECT account_id FROM accounts WHERE user_id = ?));";
		jdbcTemplate.update(cmd, getTypeId(SEND), getStatusId(isValid ? APPROVE : DENY), amount.doubleValue(), toId, fromId);
	}

	@Override
	public void requestMoney(int toId, int fromId, BigDecimal amount) {
		String cmd = "INSERT INTO transfers (transfer_type_id, transfer_status_id, amount, account_to, account_from) VALUES (?, ?, ?, "
				   + "(SELECT account_id FROM accounts WHERE user_id = ?), "
				   + "(SELECT account_id FROM accounts WHERE user_id = ?))";
		jdbcTemplate.update(cmd, getTypeId(RECEIVE), getStatusId(PENDING), amount, toId, fromId);
	}

	@Override
	public boolean updateStatus(int id, String status) {
		String cmd = "UPDATE transfers SET transfer_status_id = ? WHERE transfer_id = ?";
		return jdbcTemplate.update(cmd, getStatusId(status), id) == 1;
	}

	@Override
	public boolean approveRequest(int id, int userId) {
		if(userId != findByTransferId(id).getAccountFrom()){
			return false;
		}
		return updateStatus(id, APPROVE);
	}

	@Override
	public boolean denyRequest(int id, int userId) {
		if(userId != findByTransferId(id).getAccountFrom()){
			return false;
		}
		return updateStatus(id, DENY);
	}
	
	private List<Transfer> mapRowsetToTransfer(SqlRowSet rowSet)
	{
		List<Transfer> tlist = new ArrayList<Transfer>();
		while(rowSet.next())
		{
			tlist.add(mapRowToTransfer(rowSet));
		}
		return tlist;
	}
	
	private Transfer mapRowToTransfer(SqlRowSet rowSet){
		Transfer t = new Transfer();
		t.setStatus(validStatus.get( rowSet.getInt("transfer_type_id")));
		t.setType(validTypes.get( rowSet.getInt("transfer_type_id")));
		t.setAccountFrom(rowSet.getInt("account_from"));
		t.setAccountTo(rowSet.getInt("account_to"));
		t.setAmount(rowSet.getBigDecimal("amount"));
		t.setId(rowSet.getInt("transfer_id"));
		return t;
	}
	
}
