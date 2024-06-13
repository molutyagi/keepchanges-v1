package com.keep.changes.account;

import java.util.HashSet;
import java.util.Set;

import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(30)", nullable = false, unique = true)
	private String accountNumber;

	@Column(columnDefinition = "varchar(20)", nullable = false)
	private String ifsc;

	@Column(columnDefinition = "varchar(50)", nullable = false)
	private String bankName;

	@Column(columnDefinition = "varchar(50)", nullable = false)
	private String branch;

	@Column(columnDefinition = "varchar(50)", nullable = false)
	private String holderName;

	@ManyToOne
	@JoinTable(name = "user_account", joinColumns = @JoinColumn(name = "account", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user", referencedColumnName = "id"))
	private User holdingEntity;

	@OneToMany(mappedBy = "account")
	private Set<Fundraiser> associatedFundraisers = new HashSet<>();

	public void putUpdateAccount(Long id, String accountNumber, String ifsc, String bankName, String branch,
			String holderName) {

		this.id = id;
		this.accountNumber = accountNumber;
		this.ifsc = ifsc;
		this.bankName = bankName;
		this.branch = branch;
		this.holderName = holderName;
	}

}
