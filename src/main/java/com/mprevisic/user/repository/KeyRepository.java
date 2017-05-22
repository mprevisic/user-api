package com.mprevisic.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mprevisic.user.domain.KeyEntity;

/**
 * JPA repository for key entity
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED)
public interface KeyRepository extends JpaRepository<KeyEntity, Long> {
	
	/**
	 * Finds key with given name
	 * 
	 * @param name name of the key
	 * @return key with given name
	 */
	@Transactional(readOnly=true)
	KeyEntity findByName(String name);

}
