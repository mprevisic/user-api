package com.mprevisic.user;

/**
 * DTO for user credentials
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class Credentials {
	
	private String email;
	
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
