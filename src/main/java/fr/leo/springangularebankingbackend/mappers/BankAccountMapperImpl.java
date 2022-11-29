package fr.leo.springangularebankingbackend.mappers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import fr.leo.springangularebankingbackend.dtos.AccountHistoryDTO;
import fr.leo.springangularebankingbackend.dtos.AccountOperationDTO;
import fr.leo.springangularebankingbackend.dtos.CurrentBankAccountDto;
import fr.leo.springangularebankingbackend.dtos.CustomerDTO;
import fr.leo.springangularebankingbackend.dtos.SavingBankAccountDto;
import fr.leo.springangularebankingbackend.entities.AccountOperation;
import fr.leo.springangularebankingbackend.entities.CurrentAccount;
import fr.leo.springangularebankingbackend.entities.Customer;
import fr.leo.springangularebankingbackend.entities.SavingAccount;

@Service
public class BankAccountMapperImpl {

	public CustomerDTO fromCustomer(Customer customer ) {
		CustomerDTO customerDTO = new CustomerDTO();	
		// Solution Spring :
		BeanUtils.copyProperties(customer, customerDTO);		
		// Solution basique :
		//customerDTO.setId(customer.getId());
		//customerDTO.setEmail(customer.getEmail());
		//customerDTO.setName(customer.getName());
		// Autre solution : frameworks "MapStruct", "JMapper" !
		// Ce code, c'est du code technique, et donc on utilise des frameworks 
		//  qui permettent de génerer ce code !
		return customerDTO;
	}
	
	public Customer fromCustomerDTO( CustomerDTO customerDTO ) {		
		Customer customer = new Customer();
		// Solution Spring :
		BeanUtils.copyProperties(customerDTO, customer);
		// Solution basique :
		//customer.setId(customerDTO.getId());
		//customer.setEmail(customerDTO.getEmail());
		//customer.setName(customerDTO.getName());
		// Autre solution : framework "MapStruct", "JMapper" !
		return customer;	
	}
	
	public SavingBankAccountDto fromSavingAccount(SavingAccount savingAccount) {
		SavingBankAccountDto savingBankAccountDto = new SavingBankAccountDto();
		BeanUtils.copyProperties(savingAccount, savingBankAccountDto);		
		savingBankAccountDto.setCustomerDTO(fromCustomer(savingAccount.getCustomer()));
		savingBankAccountDto.setType(savingAccount.getClass().getSimpleName());
		return savingBankAccountDto;
	}
	
	public SavingAccount fromSavingBankAccountDto(SavingBankAccountDto savingBankAccountDto) {
		SavingAccount savingAccount = new SavingAccount();
		BeanUtils.copyProperties(savingBankAccountDto ,savingAccount );
		savingAccount.setCustomer(fromCustomerDTO(savingBankAccountDto.getCustomerDTO()));
		return savingAccount;
	}
	
	public CurrentBankAccountDto fromCurrentAccount(CurrentAccount currentAccount) {
		CurrentBankAccountDto currentBankAccountDto = new CurrentBankAccountDto();
		BeanUtils.copyProperties(currentAccount, currentBankAccountDto);
		currentBankAccountDto.setCustomerDTO(fromCustomer(currentAccount.getCustomer()));
		currentBankAccountDto.setType(currentAccount.getClass().getSimpleName());
		return currentBankAccountDto;
	}
	
	public CurrentAccount fromCurrentBankAccountDto(CurrentBankAccountDto currentBankAccountDto ) {
		CurrentAccount currentAccount = new CurrentAccount();
		BeanUtils.copyProperties(currentBankAccountDto ,currentAccount );
		currentAccount.setCustomer(fromCustomerDTO(currentBankAccountDto.getCustomerDTO()));
		return currentAccount;
	}

	
	public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation) {
		AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
		BeanUtils.copyProperties(accountOperation, accountOperationDTO);
		return accountOperationDTO;
	}
	
	public AccountOperation fromAccountOperationDTO(AccountOperationDTO accountOperationDTO) {
		AccountOperation accountOperation = new AccountOperation();
		BeanUtils.copyProperties(accountOperationDTO, accountOperation);
		return accountOperation;
	}	

}


