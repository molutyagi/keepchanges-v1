package com.keep.changes.category;

import java.util.HashSet;
import java.util.Set;

import com.keep.changes.fundraiser.Fundraiser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "varchar(100)", nullable = false, unique = true)
	private String categoryName;

	@Column(columnDefinition = "longText", nullable = false)
	private String categoryDescription;

	@Column(columnDefinition = "varchar(100)", nullable = false)
	private String categorySvg = "default.png";

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Fundraiser> fundraisers = new HashSet<>();

	public void putUpdateCategory(long id, String categoryName, String categoryDescription, String categorySvg) {

		this.id = id;
		this.categoryName = categoryName;
		this.categoryDescription = categoryDescription;
		this.categorySvg = categorySvg;
	}

	@Override
	public String toString() {
		return "Category [id=" + id + ", categoryName=" + categoryName + ", categoryDescription=" + categoryDescription
				+ ", categorySvg=" + categorySvg + "]";
	}

}
