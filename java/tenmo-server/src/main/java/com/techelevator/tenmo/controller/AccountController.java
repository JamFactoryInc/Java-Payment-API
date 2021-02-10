package com.techelevator.tenmo.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.model.BasicUser;
import com.techelevator.tenmo.model.Transfer;

@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController 
{
	
	//private final TokenProvider tokenProvider;
    //private final AuthenticationManagerBuilder authenticationManagerBuilder;
	//private JdbcTemplate jdbc = new JdbcTemplate();
    private AccountDAO accountDAO;// = new AccountSqlDAO(jdbc);
    private TransferDAO transferDAO;// = new TransferSqlDAO(jdbc);
    private UserDAO userDAO;// = new TransferSqlDAO(jdbc);
    
    
    public AccountController(AccountDAO accountDAO, TransferDAO transferDAO, UserDAO userDAO) 
    {
        this.accountDAO = accountDAO;
        this.transferDAO = transferDAO;
        this.userDAO = userDAO;
    }
   
    @RequestMapping(value="/account", method=RequestMethod.GET)
    public BigDecimal seeAccountBalance(Principal p){
    	return accountDAO.getBalance(userDAO.findIdByUsername(p.getName()));
    }
    
    @PreAuthorize("permitAll")
    @RequestMapping(value="/users", method=RequestMethod.GET)
    public BasicUser[] listUsers(){
    	BasicUser[] list = userDAO.findAllUnauthenticated().toArray(new BasicUser[0]);
    	//System.out.println(list[0].getAccount());
    	return list;
    }
    
    @RequestMapping(value="/account/send", method=RequestMethod.POST)
    public boolean sendMoney(@RequestBody Transfer request, Principal p){
    	BigDecimal currentBalance = accountDAO.getBalance(userDAO.findIdByUsername(p.getName()));
    	
    	if(currentBalance.compareTo(request.getAmount()) == -1)
    	{
    		transferDAO.sendMoney(request.getAccountTo(), userDAO.findIdByUsername(p.getName()), request.getAmount(), false);
    		return false;
    	}
    	accountDAO.addToBalance(request.getAccountTo(), request.getAmount());
    	accountDAO.addToBalance(userDAO.findIdByUsername(p.getName()), request.getAmount().negate());
    	transferDAO.sendMoney(request.getAccountTo(), userDAO.findIdByUsername(p.getName()), request.getAmount(), true);
    	return true;
    }
    
    @RequestMapping(value="/transfer", method=RequestMethod.GET)
    public Transfer[] seeAllTransfers(Principal p){
    	return fixAllNames(transferDAO.findAllByUser(userDAO.findIdByUsername(p.getName())).toArray(new Transfer[0]));
    }
    
    //readme explicitly says 'any transfer', not 'any transfer from or to their own account', so I'm taking it at its word.
    @RequestMapping(value="/transfer/{id}", method=RequestMethod.GET)
    public Transfer seeTransfer(@PathVariable int id){
    	return fixNames(transferDAO.findByTransferId(id));
    }
    
    @RequestMapping(value="/account/request", method=RequestMethod.POST)
    public boolean requestMoney(@RequestBody Transfer request, Principal p){
    	transferDAO.requestMoney(userDAO.findIdByUsername(p.getName()), request.getAccountFrom(), request.getAmount());
    	return true;
    }
    
    @RequestMapping(value="/transfer/pending", method=RequestMethod.GET)
    public Transfer[] seePendingTransfers(Principal p){
    	return fixAllNames(transferDAO.findPendingByUser(userDAO.findIdByUsername(p.getName())).toArray(new Transfer[0]));
    }
    
    @RequestMapping(value="/approve", method=RequestMethod.POST)
    public boolean approveTranfer(@RequestBody Transfer request, Principal p){
    	BigDecimal currentBalance = accountDAO.getBalance(userDAO.findIdByUsername(p.getName()));
    	
    	if(currentBalance.compareTo(request.getAmount()) == -1)
    	{
    		transferDAO.sendMoney(request.getAccountTo(), userDAO.findIdByUsername(p.getName()), request.getAmount(), false);
    		return false;
    	}
    	accountDAO.addToBalance(request.getAccountTo(), request.getAmount());
    	accountDAO.addToBalance(userDAO.findIdByUsername(p.getName()), request.getAmount().negate());
    	return transferDAO.approveRequest(request.getId(), userDAO.findIdByUsername(p.getName()));
    }
    
    @RequestMapping(value="/deny", method=RequestMethod.POST)
    public boolean denyTranfer(@RequestBody Transfer t, Principal p){
    	return transferDAO.denyRequest(t.getId(), userDAO.findIdByUsername(p.getName()));
    }
    
    private Transfer fixNames(Transfer t){
    	t.setUserFrom(userDAO.findUsernameByAccountId(t.getAccountFrom()));
    	t.setUserTo(userDAO.findUsernameByAccountId(t.getAccountTo()));
    	return t;
    }
    private Transfer[] fixAllNames(Transfer[] ts){
    	for(Transfer t : ts){
    		fixNames(t);
    	}
    	return ts;
    }
}
