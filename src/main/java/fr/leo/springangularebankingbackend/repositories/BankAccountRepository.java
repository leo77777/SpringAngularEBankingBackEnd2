package fr.leo.springangularebankingbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.leo.springangularebankingbackend.entities.BankAccount;

// Va gérer les entités SavingAccount et CurrentACcount !
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

}	
