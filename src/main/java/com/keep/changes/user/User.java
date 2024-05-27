package com.keep.changes.user;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.keep.changes.account.Account;
import com.keep.changes.address.Address;
import com.keep.changes.donation.FundraiserDonation;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.pan.Pan;
import com.keep.changes.role.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(20)", nullable = false)
	private String name;

	@Email
	@Column(columnDefinition = "varchar(30)", nullable = false, unique = true)
	private String email;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String password;

	@Column(columnDefinition = "varchar(13)", nullable = false, unique = true)
	private String phone;

	@Column(columnDefinition = "varchar(100)")
	private String displayImage = "default.png";

	@Column(columnDefinition = "varchar(100)")
	private String coverImage = "default.png";

	@Column(columnDefinition = "longtext")
	private String about;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date registerTime;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(insertable = false)
	private Date lastUpdateTime;

	private Boolean isEnabled;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role", referencedColumnName = "id"))
	private Set<Role> roles = new HashSet<>();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "user_address", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "address", referencedColumnName = "id"))
	private Address address;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "user_pan", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "pan", referencedColumnName = "id"))
	private Pan pan;

	@OneToMany(mappedBy = "holdingEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Account> accounts = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "user_fundraiser", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "fundraiser", referencedColumnName = "id"))
	private Set<Fundraiser> fundraisers = new HashSet<>();

	@OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<FundraiserDonation> donations = new HashSet<>();

	public void setUpdateUser(long id, String name, String email, String password, String phone, String displayImage,
			String coverImage, String about) {

		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.displayImage = displayImage;
		this.coverImage = coverImage;
		this.about = about;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", phone=" + phone
				+ ", displayImage=" + displayImage + ", coverImage=" + coverImage + ", about=" + about
				+ ", registerTime=" + registerTime + ", lastUpdateTime=" + lastUpdateTime + ", roles=" + roles
				+ ", address=" + address + ", pan=" + pan + ", accounts=" + accounts + ", fundraisers=" + fundraisers
				+ ", donations=" + donations + "]";
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> auth = this.roles.stream()
				.map((role) -> new SimpleGrantedAuthority(role.getRoleName())).collect(Collectors.toList());

		return auth;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
