package com.mprevisic.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Deleted user entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Entity
@Table(name = "user_blacklist")
public class UserBlacklistEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique = true)
	private String email;

	@Column(name = "date_time")
	private long dateTime;

	public long getId() {
		return id;
	}

	public UserBlacklistEntity setId(long id) {
		this.id = id;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public UserBlacklistEntity setEmail(String email) {
		this.email = email;
		return this;
	}

	public long getDateTime() {
		return dateTime;
	}

	public UserBlacklistEntity setDateTime(long dateTime) {
		this.dateTime = dateTime;
		return this;
	}

}
