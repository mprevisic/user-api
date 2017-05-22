package com.mprevisic.user.rest;

/**
 * DTO class for REST API response messages.
 * Mainly used for returning error messages. 
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class Message {
	
	private String message;
	
	public Message(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
