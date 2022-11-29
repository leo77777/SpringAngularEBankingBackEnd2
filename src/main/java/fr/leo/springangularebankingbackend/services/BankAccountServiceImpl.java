package fr.leo.springangularebankingbackend.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.leo.springangularebankingbackend.dtos.AccountHistoryDTO;
import fr.leo.springangularebankingbackend.dtos.AccountOperationDTO;
import fr.leo.springangularebankingbackend.dtos.BankAccountDto;
import fr.leo.springangularebankingbackend.dtos.CurrentBankAccountDto;
import fr.leo.springangularebankingbackend.dtos.CustomerDTO;
import fr.leo.springangularebankingbackend.dtos.SavingBankAccountDto;
import fr.leo.springangularebankingbackend.entities.AccountOperation;
import fr.leo.springangularebankingbackend.entities.BankAccount;
import fr.leo.springangularebankingbackend.entities.CurrentAccount;
import fr.leo.springangularebankingbackend.entities.Customer;
import fr.leo.springangularebankingbackend.entities.SavingAccount;
import fr.leo.springangularebankingbackend.enums.EnumAccountStatus;
import fr.leo.springangularebankingbackend.enums.EnumOperationType;
import fr.leo.springangularebankingbackend.exceptions.BalanceNotSuficientException;
import fr.leo.springangularebankingbackend.exceptions.BankAccountNotFoundException;
import fr.leo.springangularebankingbackend.exceptions.CustomerNotFoundException;
import fr.leo.springangularebankingbackend.mappers.BankAccountMapperImpl;
import fr.leo.springangularebankingbackend.repositories.AccountOperationRepository;
import fr.leo.springangularebankingbackend.repositories.BankAccountRepository;
import fr.leo.springangularebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fanny
 * Ici toutes les transactions sont transactionnelles !
 * Si il n'y a pas d'excption on fait "commit".
 * Si une exception est générée, on fait "rollback"
 */

	@Service
	@Transactional
	@AllArgsConstructor
	@Slf4j
	public class BankAccountServiceImpl implements BankAccountService {
	
	private CustomerRepository customerRepository;
	private BankAccountRepository bankAccountRepository;
	private AccountOperationRepository accountOperationRepository;	
	private BankAccountMapperImpl dtoMapper;
	
	// Ci dessous, déjà créer par Lombok !
	//Logger log=LoggerFactory.getLogger(this.getClass().getName());

	// C'est associé a une methode Post dans le restControleur,
	//  et donc ici on recoit un DTO en entrée également,
	//  pas seulement un DTO en sortie !!!
	@Override
	public CustomerDTO saveCustomer(CustomerDTO customerDto) {
		log.info("Saving new customer ...");
		Customer customer = dtoMapper.fromCustomerDTO(customerDto);
		Customer savedCustomer =  customerRepository.save(customer);
		return dtoMapper.fromCustomer(savedCustomer) ;
	}
	
	@Override
	public CustomerDTO updateCustomer(CustomerDTO customerDto) {
		log.info("Updating new customer ...");
		Customer customer = dtoMapper.fromCustomerDTO(customerDto);
		Customer savedCustomer =  customerRepository.save(customer);
		return dtoMapper.fromCustomer(savedCustomer) ;
	}
	
	@Override
	public void deleteCustomer(Long customerId) {
		log.info("Deleting customer ...");
		customerRepository.deleteById(customerId);
	}
	
	@Override
	public CurrentBankAccountDto saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
			throws CustomerNotFoundException {
		Customer customer = customerRepository.findById(customerId).orElse(null);
		if (customer == null) {
			throw new CustomerNotFoundException("Customer not found");
		}			
		CurrentAccount currentAccount = new CurrentAccount();
		currentAccount.setId(UUID.randomUUID().toString());
		currentAccount.setCreationDate(new Date());
		currentAccount.setBalance(initialBalance);
		currentAccount.setCustomer(customer);
		currentAccount.setStatus(EnumAccountStatus.CREATED);
		currentAccount.setOverDraft(overDraft);
		CurrentAccount savedBankAccount =  bankAccountRepository.save(currentAccount);		
		return dtoMapper.fromCurrentAccount(savedBankAccount) ;
	}

	@Override
	public SavingBankAccountDto saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
			throws CustomerNotFoundException {
		Customer customer = customerRepository.findById(customerId).orElse(null);
		if (customer == null) {
			throw new CustomerNotFoundException("Customer not found");
		}
		SavingAccount savingAccount = new SavingAccount();
		savingAccount.setId(UUID.randomUUID().toString());
		savingAccount.setCreationDate(new Date());
		savingAccount.setBalance(initialBalance);
		savingAccount.setCustomer(customer);
		savingAccount.setStatus(EnumAccountStatus.CREATED);
		savingAccount.setInterestRate(interestRate);
		SavingAccount savedBankAccount =  bankAccountRepository.save(savingAccount);		
		return dtoMapper.fromSavingAccount(savedBankAccount);
	}

	@Override
	public List<CustomerDTO> listCustomers() {
		List<Customer> customers =  customerRepository.findAll();		
		// Programmation fonctionnelle !
		List<CustomerDTO> customerDTOs = customers.stream().map(cust->dtoMapper
				.fromCustomer(cust))			
				.collect(Collectors
				.toList());		
		/* Programmation impérative <=> classique
		List<CustomerDTO> customerDTOs = new ArrayList<>();
		for (Customer customer : customers) {
			CustomerDTO customerDTO = dtoMapper.fromCustomer(customer);
			customerDTOs.add(customerDTO);
		}*/
		return customerDTOs;
	}

	@Override
	public BankAccountDto getBankAccount(String accountId) throws BankAccountNotFoundException {
		BankAccount bankAccount = bankAccountRepository.findById(accountId)
				.orElseThrow( ()->new BankAccountNotFoundException("BankAccount not found"));
		if (bankAccount instanceof  SavingAccount) {
			return dtoMapper.fromSavingAccount((SavingAccount) bankAccount);
		}else {
			CurrentAccount currentAccount = (CurrentAccount) bankAccount;
			return dtoMapper.fromCurrentAccount(currentAccount);
		}
	}

	// C'est inutil de modifier les méthodes debit() et credit(), car elles
	//  ne retournent rien , et en entrée elle ne prennent que des types simples
	//  Il n'y a pas de Dto !
	@Override
	public void debit(String accountI, double amount, String description) throws BankAccountNotFoundException, BalanceNotSuficientException {
		BankAccount bankAccount = bankAccountRepository.findById(accountI)
				.orElseThrow( ()->new BankAccountNotFoundException("BankAccount not found"));
		if (bankAccount.getBalance() < amount) {
			throw new BalanceNotSuficientException("Balance not suficient");
		}
		AccountOperation accountOperation = new AccountOperation();
		accountOperation.setType(EnumOperationType.DEBIT);
		accountOperation.setAmount(amount);
		accountOperation.setDescription(description);
		accountOperation.setOperationDate(new Date());
		accountOperation.setBankAccount(bankAccount);
		accountOperationRepository.save(accountOperation); 
		bankAccount.setBalance(bankAccount.getBalance() - amount);
		bankAccountRepository.save(bankAccount);
	}

	@Override
	public void credit(String accountI, double amount, String description) throws BankAccountNotFoundException {
		BankAccount bankAccount = bankAccountRepository.findById(accountI)
				.orElseThrow( ()->new BankAccountNotFoundException("BankAccount not found"));
		AccountOperation accountOperation = new AccountOperation();
		accountOperation.setType(EnumOperationType.CREDIT);
		accountOperation.setBankAccount(bankAccount);
		accountOperation.setAmount(amount);
		accountOperation.setDescription(description);
		accountOperation.setOperationDate(new Date());
		accountOperationRepository.save(accountOperation); 
		bankAccount.setBalance(bankAccount.getBalance() + amount);
		bankAccountRepository.save(bankAccount);
	}

	@Override
	public void transfert(String AccountIdSource, String AccountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSuficientException {
		debit(AccountIdSource, amount, "Transfert to " + AccountIdDestination);
		credit(AccountIdDestination, amount, "Transfert from " + AccountIdSource);
	}
	
	@Override
	public List<BankAccountDto> bankAccountList(){
		List<BankAccount> bankAccounts = bankAccountRepository.findAll();
		List<BankAccountDto> bankAccountDtos =  bankAccounts.stream().map(bankAccount->{
			if (bankAccount instanceof SavingAccount) {
				SavingAccount savingAccount = (SavingAccount) bankAccount;
				return dtoMapper.fromSavingAccount(savingAccount);
			}else {
				CurrentAccount currentAccount  = (CurrentAccount) bankAccount;
				return dtoMapper.fromCurrentAccount(currentAccount);
			}
		}).collect(Collectors.toList());
		return bankAccountDtos;
	}
	
	@Override
	public CustomerDTO getCustomer(Long idCustomer) throws CustomerNotFoundException{
		Customer customer =  customerRepository
				.findById(idCustomer)
				.orElseThrow(()-> new CustomerNotFoundException("Customer introuvable !") );
		return dtoMapper.fromCustomer(customer);
	}

	@Override
	public List<AccountOperationDTO> accountHistory(String accountId) {
		List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
		List<AccountOperationDTO> accountOperationDTOs = accountOperations.stream().map( op->{
			return dtoMapper.fromAccountOperation(op);
		}).collect(Collectors.toList());
		return accountOperationDTOs;
	}
	
	@Override
	public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
		
		BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
		if (bankAccount==null) {
			throw new BankAccountNotFoundException("Account not find");
		}
		Page<AccountOperation> accountOperations = accountOperationRepository
					.findByBankAccountId(accountId,PageRequest.of(page, size));
		
		AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
		List<AccountOperationDTO> accountOperationDTOs =
						accountOperations.getContent()
						.stream()
						.map(op->dtoMapper.fromAccountOperation(op))
						.collect(Collectors.toList());
		// Usually => Mapper Work
		accountHistoryDTO.setAccountOperationDTOs(accountOperationDTOs);
		accountHistoryDTO.setAccountId(bankAccount.getId());
		accountHistoryDTO.setBalance(bankAccount.getBalance());
		accountHistoryDTO.setCurrentPage(page);
		accountHistoryDTO.setTotalPages(accountOperations.getTotalPages() );
		accountHistoryDTO.setPageSize(size);
		// Usually => Mapper Work !
		return accountHistoryDTO;
	}
}
