package fr.leo.springangularebankingbackend;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

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
import fr.leo.springangularebankingbackend.repositories.AccountOperationRepository;
import fr.leo.springangularebankingbackend.repositories.BankAccountRepository;
import fr.leo.springangularebankingbackend.repositories.CustomerRepository;
import fr.leo.springangularebankingbackend.services.BankAccountService;
import fr.leo.springangularebankingbackend.services.BankAccountServiceImpl;
import fr.leo.springangularebankingbackend.services.BankService;

@SpringBootApplication
public class SpringAngularEBankingBackEnd2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringAngularEBankingBackEnd2Application.class, args);
	}
	
		// CREATION DES DONNEES:
		// @Bean
		CommandLineRunner start1(CustomerRepository customerRepository,
								AccountOperationRepository accountOperationRepository,
								BankAccountRepository accountRepository) {
			return args->{
				// Création des clients
				Stream.of("Joe","Averel","Rantanplan").forEach(name->{
					Customer customer = new Customer(null, name, name+"@free.fr", null);
					customerRepository.save(customer);
					});
				
				// Création de 2 types de comptes pour chaque client
				customerRepository.findAll().forEach(cust->{
					CurrentAccount currentAccount = new CurrentAccount();
					currentAccount.setId(UUID.randomUUID().toString());
					currentAccount.setCustomer(cust);
					currentAccount.setBalance( Math.random()*100000);
					currentAccount.setCreationDate(new Date());
					currentAccount.setStatus(EnumAccountStatus.CREATED);
					currentAccount.setOverDraft(10000);
					accountRepository.save(currentAccount);
					
					SavingAccount savingAccount = new SavingAccount();
					savingAccount.setId(UUID.randomUUID().toString());
					savingAccount.setCustomer(cust);
					savingAccount.setBalance( Math.random()*100000);
					savingAccount.setCreationDate(new Date());
					savingAccount.setStatus(EnumAccountStatus.CREATED);
					savingAccount.setInterestRate(5.5);
					accountRepository.save(savingAccount);
				}); 
				
				// On creer 5 operations pour chaque comptes
				accountRepository.findAll().forEach( account->{
					for (int i = 0; i < 5; i++) {
						AccountOperation accountOperation = new AccountOperation(
									null, 
									new Date(),
									Math.random() * 120000,
									Math.random()>0.5 ? EnumOperationType.DEBIT : EnumOperationType.CREDIT  ,
									account,
									"Nouveau compte !");
						accountOperationRepository.save(accountOperation);
					}	
				});
			};
		}

		// DISPLAY DES DONNEES:
		// @Bean
		CommandLineRunner start(BankAccountRepository accountRepository	) {
			return args->{	
				BankAccount bankAccount = accountRepository.
						findById("12d60ec3-3b71-4ae9-b502-f9a6ecb6094c")
						.orElse(null);
				System.out.println("********************");	
				System.out.println(bankAccount);
				System.out.println(bankAccount.getId());
				System.out.println(bankAccount.getBalance());
				System.out.println(bankAccount.getStatus());
				System.out.println(bankAccount.getCreationDate());
				System.out.println(bankAccount.getCustomer().getName());
				if (bankAccount instanceof CurrentAccount) {
					CurrentAccount ca = (CurrentAccount) bankAccount;
					System.out.println(ca.getClass().getSimpleName()+ " : " + ca.getOverDraft());					
				}else {
					SavingAccount sa = (SavingAccount) bankAccount;
					System.out.println(sa.getClass().getSimpleName() + " : " +  
					sa.getInterestRate());
				}
				System.out.println("Liste des opérations : ");
				bankAccount.getAccountOperations().forEach(op->{
					System.out.print(op.getDescription() + "\t" 
							+ op.getAmount() +"\t"
							+ op.getOperationDate() +"\t"
							+ (op.getType() + "\n"));
				});			
			};
		}
		
		// DISPLAY DES DONNEES:
		@Bean
		CommandLineRunner start(BankAccountService bankAccountService	) {
			return args->{								
				// bankService.consulter();
				Stream.of("Joe2","Averel2","Rantanplan2").forEach(name->{
					// On créé 3 clients
					CustomerDTO customer = new CustomerDTO();
					customer.setName(name);
					customer.setEmail( name+"@free.fr");
					bankAccountService.saveCustomer(customer);
				});
				
				bankAccountService.listCustomers().forEach(customer->{
					try {
						// On créé un compte epargne et un compte courant
						// pour chacun des 3 clients
						bankAccountService.saveCurrentBankAccount(
								Math.random()*100000,
								9000, 
								customer.getId());
						bankAccountService.saveSavingBankAccount(
									Math.random()*100000,
									5.5,
									customer.getId());
					} catch (CustomerNotFoundException e) {
						e.printStackTrace();
					}
				});
				
				// On créé 10 opérations pour chaque comptes
				List<BankAccountDto> bankAccounts = bankAccountService.bankAccountList(); 
				for(BankAccountDto bankAccount : bankAccounts) {
					for (int i = 0; i < 10; i++) {
						String accountId;
						if (bankAccount instanceof SavingBankAccountDto) {
							accountId = ((SavingBankAccountDto) bankAccount).getId();
						}else {
							accountId = ((CurrentBankAccountDto) bankAccount).getId();
						}
						bankAccountService.credit(accountId ,
								10000 + Math.random()*120000, "Credit");
						bankAccountService.debit(accountId ,
								1000 + Math.random()*12000, "Debit");
					}
				}	
				
			};
		}
		
}
