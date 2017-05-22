package com.mprevisic.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.mprevisic.user.dto.UserDto;

/**
 * User entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Entity
@Table(name="users")
public class UserEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique=true)
	private String email;
	
	private String password;
	
	private String firstName;
	
	private String lastName;
	
	private String title;
	
	private String phoneCode;
	
	private String phoneNumber;
	
	private Integer role;

	public long getId() {
		return id;
	}

	public UserEntity setId(long id) {
		this.id = id;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public UserEntity setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public UserEntity setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public UserEntity setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public UserEntity setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public UserEntity setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getPhoneCode() {
		return phoneCode;
	}

	public UserEntity setPhoneCode(String phoneCode) {
		this.phoneCode = phoneCode;
		return this;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public UserEntity setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}
	
	public Integer getRole() {
		return role;
	}
	
	public UserEntity setRole(Integer role) {
		this.role = role;
		return this;
	}
	
	public UserDto toDto() {
		UserDto dto = new UserDto().setUserId(id).setEmail(email).setFirstName(firstName)
				.setLastName(lastName).setPhoneCode(phoneCode).setPhoneNumber(phoneNumber)
				.setRole(role).setTitle(title).setPassword(password);
		return dto;
	}
	
	public static UserEntity fromDto(UserDto dto) {
		UserEntity entity = new UserEntity().setId(dto.getUserId()).setEmail(dto.getEmail())
				.setFirstName(dto.getFirstName()).setLastName(dto.getLastName()).setPhoneCode(dto.getPhoneCode())
				.setPhoneNumber(dto.getPhoneNumber()).setRole(dto.getRole()).setTitle(dto.getTitle())
				.setPassword(dto.getPassword());
		return entity;
	}

}
