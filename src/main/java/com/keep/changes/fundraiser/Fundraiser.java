package com.keep.changes.fundraiser;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.keep.changes.account.Account;
import com.keep.changes.address.Address;
import com.keep.changes.category.Category;
import com.keep.changes.donation.Donation;
import com.keep.changes.pan.Pan;
import com.keep.changes.photo.Photo;
import com.keep.changes.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Fundraiser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String fundraiserTitle;

	@Column(columnDefinition = "longtext")
	private String fundraiserDescription;

	@Column(columnDefinition = "varchar(100)")
	private String cause;

	private double raiseGoal;

	private double raised;

	@Email
	@Column(columnDefinition = "varchar(30)")
	private String email;

	@Column(columnDefinition = "varchar(13)")
	private String phone;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedDate;

	@Column(columnDefinition = "varchar(100)")
	private String displayPhoto;

	@Column(columnDefinition = "varchar(100)")
	private String coverPhoto;

	private boolean isActive;

	@Enumerated(EnumType.STRING)
	private AdminApproval approval;

	@Enumerated(EnumType.STRING)
	private FundraiserStatus status;

	@ManyToOne
	private Category category;

	@ManyToOne
	private User postedBy;

	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Photo> photos = new HashSet<>();

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "fundraiser_address", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "address", referencedColumnName = "id"))
	private Address address;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "fundraiser_pan", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "pan", referencedColumnName = "id"))
	private Pan pan;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "fundraiser_account", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "account", referencedColumnName = "id"))
	private Account account;

	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Donation> donations = new HashSet<>();

	public void putUpdateFundraiser(long id, String fundraiserTitle, String fundraiserDescription, String cause,
			double raiseGoal, String email, String phone, Date endDate, String displayPhoto, String coverPhoto) {

		this.id = id;
		this.fundraiserTitle = fundraiserTitle;
		this.fundraiserDescription = fundraiserDescription;
		this.cause = cause;
		this.raiseGoal = raiseGoal;
		this.email = email;
		this.phone = phone;
		this.endDate = endDate;
		this.displayPhoto = displayPhoto;
		this.coverPhoto = coverPhoto;
	}

}
