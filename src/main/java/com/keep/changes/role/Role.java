package com.keep.changes.role;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keep.changes.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role {

	@Id
	private long id;

	@Column(unique = true, nullable = false)
	private String roleName;

	private String roleDescription;

	@ManyToMany(mappedBy = "roles")
	@JsonIgnore
	private Set<User> users;

	public Role(long id, String roleName, String roleDescription) {
		super();
		this.id = id;
		this.roleName = roleName;
		this.roleDescription = roleDescription;
	}

}
