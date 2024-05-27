package com.keep.changes.fundraiser.photo;

import com.keep.changes.fundraiser.Fundraiser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Photo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String photoUrl;

	@ManyToOne
	private Fundraiser fundraiser;

	@Override
	public String toString() {
		return "Photo [id=" + id + ", photoUrl=" + photoUrl + "]";
	}

}
