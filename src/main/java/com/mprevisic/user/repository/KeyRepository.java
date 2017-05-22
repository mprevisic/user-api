package com.mprevisic.user.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mprevisic.user.domain.KeyEntity;

/**
 * JPA repository for key entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Repository
@Transactional
public interface KeyRepository extends JpaRepository<KeyEntity, Long> {
	
	/**
	 * Finds key with given name
	 * 
	 * @param name name of the key
	 * @return key with given name
	 */
	KeyEntity findByName(String name);

}
