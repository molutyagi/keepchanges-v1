package com.keep.changes.fundraiser;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.keep.changes.account.Account;
import com.keep.changes.address.Address;
import com.keep.changes.category.Category;
import com.keep.changes.donation.FundraiserDonation;
import com.keep.changes.fundraiser.document.FundraiserDocument;
import com.keep.changes.fundraiser.photo.Photo;
import com.keep.changes.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(FundraiserEntityListener.class)
public class Fundraiser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String fundraiserTitle;

	@Column(columnDefinition = "longtext")
	private String fundraiserDescription;

	@Column(columnDefinition = "varchar(100)")
	private String beneficiary;

	private Double raiseGoal;

	private Double raised = 0.0;

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
	@DateTimeFormat(pattern = "dd-MM-yyyy")
	private Date endDate;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedDate;

	@Column(columnDefinition = "varchar(100)")
	private String displayPhoto;

	private boolean isActive = false;

	@Enumerated(EnumType.STRING)
	private AdminApproval approval;

	@Column(columnDefinition = "longtext")
	private String adminRemarks;

	@Enumerated(EnumType.STRING)
	private FundraiserStatus status;

	@ManyToOne
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	private User postedBy;

	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Photo> photos = new HashSet<>();
	
	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FundraiserDocument> documents = new HashSet<>();

	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FundraiserDocument> documents = new HashSet<>();

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "fundraiser_address", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "address", referencedColumnName = "id"))
	private Address address;

//	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinTable(name = "fundraiser_pan", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "pan", referencedColumnName = "id"))
//	private Pan pan;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinTable(name = "fundraiser_account", joinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "account", referencedColumnName = "id"))
	private Account account;

	@OneToMany(mappedBy = "fundraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FundraiserDonation> donations = new HashSet<>();

	public void putUpdateFundraiser(Long id, String fundraiserTitle, String fundraiserDescription, String beneficiary,
			double raiseGoal, String email, String phone, Date endDate, String displayPhoto, String coverPhoto) {

		this.id = id;
		this.fundraiserTitle = fundraiserTitle;
		this.fundraiserDescription = fundraiserDescription;
		this.beneficiary = beneficiary;
		this.raiseGoal = raiseGoal;
		this.email = email;
		this.phone = phone;
		this.endDate = endDate;
		this.displayPhoto = displayPhoto;
	}

	@Override
	public String toString() {
		return "Fundraiser [id=" + id + ", fundraiserTitle=" + fundraiserTitle + ", fundraiserDescription="
				+ fundraiserDescription + ", beneficiary=" + beneficiary + ", raiseGoal=" + raiseGoal + ", raised="
				+ raised + ", email=" + email + ", phone=" + phone + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", lastModifiedDate=" + lastModifiedDate + ", displayPhoto=" + displayPhoto + ", isActive=" + isActive
				+ ", approval=" + approval + ", status=" + status + ", category=" + category + ", postedBy=" + postedBy
				+ ", photos=" + photos + ", address=" + address + ", account=" + account + ", donations=" + donations
				+ "]";
	}

}
