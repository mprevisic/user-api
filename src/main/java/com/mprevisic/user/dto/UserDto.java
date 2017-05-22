package com.mprevisic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * User DTO class
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class UserDto {
	
	private long userId;
	
	private String email;

	@JsonProperty(access=Access.WRITE_ONLY)
	private String password;
	
	private String firstName;
	
	private String lastName;
	
	private String title;
	
	private String phoneCode;
	
	private String phoneNumber;
	
	private Integer role;

	public long getUserId() {
		return userId;
	}

	public UserDto setUserId(long id) {
		this.userId = id;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public UserDto setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public UserDto setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public UserDto setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public UserDto setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public UserDto setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getPhoneCode() {
		return phoneCode;
	}

	public UserDto setPhoneCode(String phoneCode) {
		this.phoneCode = phoneCode;
		return this;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public UserDto setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public Integer getRole() {
		return role;
	}

	public UserDto setRole(Integer role) {
		this.role = role;
		return this;
	}

}
