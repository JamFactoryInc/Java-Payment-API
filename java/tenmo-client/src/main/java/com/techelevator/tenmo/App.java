package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.text.ParseException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.view.ConsoleService;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {
		BigDecimal balance = restTemplate
                .exchange(API_BASE_URL + "account", HttpMethod.GET, makeAccountEntity(), BigDecimal.class).getBody();
		System.out.println(String.format("Your current account balance is: $%.2f", balance));
	}

	private void viewTransferHistory() {
		Transfer[] transfers = restTemplate
                .exchange(API_BASE_URL + "transfer", HttpMethod.GET, makeAccountEntity(), Transfer[].class).getBody();
		System.out.println("-------------------------------------------");
		if(transfers.length == 0){
			System.out.println("There are no transfers");
			System.out.println("-------------------------------------------");
		}else{
			System.out.println("Transfers");
			System.out.println("ID\tFrom/To\t\t\tAmount");
			System.out.println("-------------------------------------------");
			for(Transfer t : transfers){
				if(t.getUserFrom().equals(currentUser.getUser().getUsername())){
					System.out.println(String.format("%d\tTo:\t%s\t\t$%.2f",t.getId(),t.getUserTo(),t.getAmount()));
				}else{
					System.out.println(String.format("%d\tFrom:\t%s\t\t$%.2f",t.getId(),t.getUserFrom(),t.getAmount()));
				}
			}
			System.out.println("---------");
			int selection = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
			while(selection != 0){
				for(Transfer t : transfers){
					if(t.getId() == selection){
						getTransferDetails(t);
						selection = 0;
						break;
					}
				}
				if(selection != 0){
					selection = console.getUserInputInteger("No transfer found with given ID. Please try again (0 to cancel)");
				}
			}
		}
	}
	
	private void getTransferDetails(Transfer t){
		System.out.println("--------------------------------------------");
		System.out.println("Transfer Details");
		System.out.println("--------------------------------------------");
		System.out.println(String.format("Id: %d", t.getId()));
		System.out.println(String.format("From: %s", t.getUserFrom()));
		System.out.println(String.format("To: %s", t.getUserTo()));
		System.out.println(String.format("Type: %s", t.getType()));
		System.out.println(String.format("Status: %s", t.getStatus()));
		System.out.println(String.format("Amount: %.2f", t.getAmount()));
	}

	private void viewPendingRequests() {
		Transfer[] transfers = restTemplate
                .exchange(API_BASE_URL + "transfer/pending", HttpMethod.GET, makeAccountEntity(), Transfer[].class).getBody();
		
		System.out.println("-------------------------------------------");
		if(transfers.length == 0){
			System.out.println("There are no transfers");
			System.out.println("-------------------------------------------");
		}else{
			System.out.println("Pending Transfers");
			System.out.println("ID\tTo\t\tAmount");
			System.out.println("-------------------------------------------");
			for(Transfer t : transfers){
				System.out.println(String.format("%d\t%s\t\t$%.2f",t.getId(),t.getUserTo(),t.getAmount()));
			}
			System.out.println("---------");
			int selection = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
			while(selection != 0){
				for(Transfer t : transfers){
					if(t.getId() == selection){
						getPendingDetails(t);
						selection = 0;
						break;
					}
				}
				if(selection != 0){
					selection = console.getUserInputInteger("No transfer found with given ID. Please try again (0 to cancel)");
				}
			}
		}
	}
	
	private void getPendingDetails(Transfer t){
		System.out.println("1: Approve");
		System.out.println("2: Reject");
		System.out.println("0: Don't approve or reject");
		System.out.println("---------");
		int choice = console.getUserInputInteger("Please choose an option");
		while(choice < 0 || choice > 2){
			choice = console.getUserInputInteger("Please choose one of the available options");
		}
		if(choice == 1){
			Boolean success = restTemplate
					.exchange(API_BASE_URL + "approve", HttpMethod.POST, makeAccountTransferEntity(t), Boolean.class).getBody();
			if(success){
				System.out.println("Transaction successfully approved.");
			}else{
				System.out.println("Transaction could not be approved (check account balance).");
			}
		}
		if(choice == 2){
			Boolean success = restTemplate
		            .exchange(API_BASE_URL + "deny", HttpMethod.POST, makeAccountTransferEntity(t), Boolean.class).getBody();
			if(success){
				System.out.println("Transaction successfully denied.");
			}else{
				System.out.println("Transaction could not be denied (communication error?).");
			}
		}
	}

	private void sendBucks() {
		User to = selectUser("Enter ID of user you are sending to (0 to cancel)");
		if(to != null){
			Transfer t = new Transfer();
			t.setAccountFrom(currentUser.getUser().getId());
			t.setAccountTo(to.getId());
			BigDecimal amount = console.getUserInputMoney("Enter amount");
			while(amount.compareTo(BigDecimal.ZERO) < 0){
				amount = console.getUserInputMoney("Please enter a positive amount of money");
			}
			t.setAmount(amount);
			Boolean success = restTemplate
	        	.exchange(API_BASE_URL + "account/send", HttpMethod.POST, makeAccountTransferEntity(t), Boolean.class).getBody();
			if(success){
				System.out.println("Transfer successful.");
			}else{
				System.out.println("Transfer unsuccessful (check account balance).");
			}
		}
	}
	
	private User selectUser(String prompt){
		System.out.println("-------------------------------------------");
		System.out.println("Users");
		System.out.println("ID\tName");
		System.out.println("-------------------------------------------");
		User[] users = restTemplate
                .exchange(API_BASE_URL + "users", HttpMethod.GET, makeAccountEntity(), User[].class).getBody();
		for(User u : users){
			System.out.println(String.format("%d\t%s", u.getId(), u.getUsername()));
		}
		System.out.println("---------");
		System.out.println();
		int id = console.getUserInputInteger(prompt);
		while(id != 0){
			if(id == currentUser.getUser().getId()){
				id = console.getUserInputInteger("Please choose a user other than yourself (0 to cancel)");
			}else{
				for(User u : users){
					if(u.getId() == id){
						return u;
					}
				}
				id = console.getUserInputInteger("No user found with given ID. Please enter a valid user ID (0 to cancel)");
			}
		}
		return null;
	}
	
	private void requestBucks() {
		User from = selectUser("Enter ID of user you are requesting from (0 to cancel)");
		if(from != null){
			Transfer t = new Transfer();
			t.setAccountTo(currentUser.getUser().getId());
			t.setAccountFrom(from.getId());
			BigDecimal amount = console.getUserInputMoney("Enter amount");
			while(amount.compareTo(BigDecimal.ZERO) < 0){
				amount = console.getUserInputMoney("Please enter a positive amount of money");
			}
			t.setAmount(amount);
			Boolean success = restTemplate
	        	.exchange(API_BASE_URL + "account/request", HttpMethod.POST, makeAccountTransferEntity(t), Boolean.class).getBody();
			if(success){
				System.out.println("Transfer successful.");
			}else{
				System.out.println("Transfer unsuccessful (communication error?).");
			}
		}
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
	
	private HttpEntity makeAccountEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }
	private HttpEntity<Transfer> makeAccountTransferEntity(Transfer t) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Transfer> entity = new HttpEntity<Transfer>(t, headers);
        return entity;
    }
}
