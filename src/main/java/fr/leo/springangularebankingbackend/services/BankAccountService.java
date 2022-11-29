package fr.leo.springangularebankingbackend.services;

import java.util.List;

import fr.leo.springangularebankingbackend.dtos.AccountHistoryDTO;
import fr.leo.springangularebankingbackend.dtos.AccountOperationDTO;
import fr.leo.springangularebankingbackend.dtos.BankAccountDto;
import fr.leo.springangularebankingbackend.dtos.CurrentBankAccountDto;
import fr.leo.springangularebankingbackend.dtos.CustomerDTO;
import fr.leo.springangularebankingbackend.dtos.SavingBankAccountDto;
import fr.leo.springangularebankingbackend.exceptions.BalanceNotSuficientException;
import fr.leo.springangularebankingbackend.exceptions.BankAccountNotFoundException;
import fr.leo.springangularebankingbackend.exceptions.CustomerNotFoundException;

public interface BankAccountService {
	
	CustomerDTO saveCustomer(CustomerDTO customerDto);
	CurrentBankAccountDto saveCurrentBankAccount( double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
	SavingBankAccountDto saveSavingBankAccount( double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
	List<CustomerDTO> listCustomers();
	BankAccountDto getBankAccount(String accountId) throws BankAccountNotFoundException;
	void debit(String accountI, double amount,String description ) throws BankAccountNotFoundException, BalanceNotSuficientException;
	void credit(String accountI, double amount,String description ) throws BankAccountNotFoundException;
	void transfert(String AccountIdSource, String AccountIdDestination, double amount ) throws BankAccountNotFoundException, BalanceNotSuficientException;
	List<BankAccountDto> bankAccountList();
	CustomerDTO getCustomer(Long idCustomer) throws CustomerNotFoundException;
	CustomerDTO updateCustomer(CustomerDTO customerDto);
	void deleteCustomer(Long customerId);
	List<AccountOperationDTO> accountHistory(String accountId);
	AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;

}
