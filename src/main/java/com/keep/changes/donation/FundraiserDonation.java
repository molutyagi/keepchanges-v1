package com.keep.changes.donation;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FundraiserDonation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private Double donationAmount;

	@Column(nullable = false, updatable = false, unique = true)
	private String transactionId;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, updatable = false)
	private Date donationDate;

	@ManyToOne
	@JoinColumn(name = "donor_id")
	private User donor;

	@ManyToOne
	@JoinColumn(name = "fundraiser_id")
	private Fundraiser fundraiser;

	public void putUpdateDonation(Long id, Double donationAmount, String transactionId) {
		this.id = id;
		this.donationAmount = donationAmount;
		this.transactionId = transactionId;
	}

}
