package hu.lacztam.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "role_names")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
	@Id
	@GeneratedValue
	@Column(name = "role_id", unique = true)
	private long id;
	@Column(name = "rolename")
	private String rolename;

	public UserRole(String rolename) {
		this.rolename = rolename;
	}

}
