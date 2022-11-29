package fr.leo.springangularebankingbackend.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.leo.springangularebankingbackend.enums.EnumAccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", length = 4, discriminatorType = DiscriminatorType.STRING)
@Data @AllArgsConstructor @NoArgsConstructor
public abstract class BankAccount {	
	
	@Id // N'est pas generer automatiquement, c'est la banque qui le genere ! C'est un String 
	private String id;
	private double balance;
	private Date creationDate;
	
	@Enumerated(EnumType.STRING)
	private EnumAccountStatus status;
	
	@ManyToOne // Plusieurs comptes concerne le meme client
	private Customer customer;
	
	@OneToMany(mappedBy = "bankAccount" , fetch = FetchType.LAZY)
	private List<AccountOperation> accountOperations;

}
