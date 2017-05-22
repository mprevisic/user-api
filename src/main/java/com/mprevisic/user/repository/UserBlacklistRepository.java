package com.mprevisic.user.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mprevisic.user.domain.UserBlacklistEntity;

/**
 * JPA repository for user blacklist entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Repository
@Transactional
public interface UserBlacklistRepository extends JpaRepository<UserBlacklistEntity, Long> {

	/**
	 * Deletes user with given e-mail address from the blacklist
	 * @param email e-mail address of the user removed from the blacklist
	 */
	void deleteByEmail(String email);
}
