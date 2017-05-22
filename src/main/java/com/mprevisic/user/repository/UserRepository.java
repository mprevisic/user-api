package com.mprevisic.user.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mprevisic.user.domain.UserEntity;

/**
 * JPA repository for user entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	
	/**
	 * Finds user with given e-mail address
	 * 
	 * @param email e-mail of the user
	 * @return user with given e-mail address
	 */
	Optional<UserEntity> findByEmail(String email);

}
