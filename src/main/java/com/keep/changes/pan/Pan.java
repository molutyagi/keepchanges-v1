package com.keep.changes.pan;

import com.keep.changes.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Pan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(20)", nullable = false, unique = true)
	private String panNumber;

	@Column(columnDefinition = "varchar(30)", nullable = false)
	private String nameOnPan;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String address;

	@Column(columnDefinition = "varchar(30)", nullable = false)
	private String city;

	@Column(columnDefinition = "varchar(50)", nullable = false)
	private String state;

	@Column(columnDefinition = "varchar(30)", nullable = false)
	private String country;

	@Column(columnDefinition = "varchar(20)", nullable = false)
	private String pincode;

	@OneToOne
	private User panHolder;

//	@OneToMany(mappedBy = "pan")
//	private Set<Fundraiser> fundraisers;

	public void putUpdatePan(Long id, String panNumber, String nameOnPan, String address, String city, String state,
			String country, String pincode) {

		this.id = id;
		this.panNumber = panNumber;
		this.nameOnPan = nameOnPan;
		this.address = address;
		this.city = city;
		this.state = state;
		this.country = country;
		this.pincode = pincode;
	}

}
