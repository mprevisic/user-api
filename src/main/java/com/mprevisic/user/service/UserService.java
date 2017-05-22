package com.mprevisic.user.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mprevisic.user.Credentials;
import com.mprevisic.user.domain.UserEntity;
import com.mprevisic.user.dto.UserDto;
import com.mprevisic.user.repository.UserRepository;
import com.mprevisic.user.util.UserBlacklistCache;

/**
 * Service for managing users
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Service
@Transactional(propagation=Propagation.REQUIRED)
public class UserService {

	private UserRepository userRepo;

	private UserBlacklistCache deletedUserCache;

	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Autowired
	public UserService(UserRepository userRepo, UserBlacklistCache deletedUserCache) {
		this.userRepo = userRepo;
		this.deletedUserCache = deletedUserCache;
	}

	/**
	 * Authenticates the user by checking the credentials
	 * 
	 * @param credentials
	 *            credentials of the user (e-mail and password)
	 * @return true if authenticated, false if not
	 */
	@Transactional(readOnly=true)
	public boolean authenticate(Credentials credentials) {
		Optional<UserEntity> user = userRepo.findByEmail(credentials.getEmail());

		if (!user.isPresent() || !BCrypt.checkpw(credentials.getPassword(), user.get().getPassword())) {
			return false;
		}

		return true;
	}

	/**
	 * Returns user with given e-mail address
	 * 
	 * @param email
	 *            e-mail address of user
	 * @return user with given e-mail address
	 */
	@Transactional(readOnly=true)
	public Optional<UserDto> findByEmail(String email) {
		Optional<UserEntity> ue = userRepo.findByEmail(email);
		if (ue.isPresent()) {
			return Optional.of(ue.get().toDto());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Returns user with given ID
	 * 
	 * @param userId
	 *            ID of the user
	 * @return user with given ID
	 */
	@Transactional(readOnly=true)
	public Optional<UserDto> findById(long userId) {
		UserEntity ue = userRepo.findOne(userId);
		if (ue != null) {
			return Optional.of(ue.toDto());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Saves new user or updates existing one
	 */
	public UserDto saveUser(UserDto user) {
		UserEntity ue = UserEntity.fromDto(user);

		if (ue.getPassword() == null || ue.getPassword().isEmpty()) {
			throw new IllegalArgumentException("Password is missing!");
		}

		String encryptedPassword = passwordEncoder.encode(ue.getPassword());
		ue.setPassword(encryptedPassword);

		Optional<UserEntity> existingUser = userRepo.findByEmail(user.getEmail());

		if (existingUser.isPresent() && user.getUserId() != existingUser.get().getId()) {
			throw new IllegalArgumentException("Another user already has the given e-mail address!");
		}

		ue = userRepo.save(ue);

		/**
		 * if a previously deleted user is registered again, delete him from the
		 * black-list to enable access
		 */
		if (deletedUserCache.checkUserDeleted(ue.getEmail())) {
			deletedUserCache.removeFromCache(ue.getEmail());
		}

		return ue.toDto();
	}

	/**
	 * Updates a user
	 * 
	 * @param user
	 * @param newData
	 * @return
	 */
	public UserDto updateUser(final UserDto user, final Map<String, Object> newData) {
		if (newData.containsKey("email")) {
			updateEmail(newData, user);
		}

		if (newData.containsKey("firstName")) {
			user.setFirstName((String) newData.get("firstName"));
		}

		if (newData.containsKey("lastName")) {
			user.setLastName((String) newData.get("lastName"));
		}

		if (newData.containsKey("title")) {
			user.setTitle((String) newData.get("title"));
		}

		if (newData.containsKey("phoneCode")) {
			user.setPhoneCode((String) newData.get("phoneCode"));
		}

		if (newData.containsKey("phoneNumber")) {
			user.setPhoneNumber((String) newData.get("phoneNumber"));
		}

		return saveUser(user);
	}

	/**
	 * Updates e-mail of a user.
	 * 
	 * @throws IllegalArgumentException
	 *             if another user already has the updated e-mail address
	 */
	private String updateEmail(final Map<String, Object> newData, UserDto user) {
		String newEmail = (String) newData.get("email");
		if (!newEmail.equals(user.getEmail())) {
			Optional<UserDto> otherUser = findByEmail(newEmail);
			if (otherUser.isPresent() && otherUser.get().getUserId() != user.getUserId()) {
				throw new IllegalArgumentException("Another user already has the given e-mail address!");
			}
			user.setEmail(newEmail);
		}

		return null;
	}

	/**
	 * Deletes user with given ID
	 */
	public void deleteUser(final long userId) {
		UserEntity user = userRepo.findOne(userId);
		deletedUserCache.addDeletedUser(user.getEmail());
		userRepo.delete(userId);
	}

}
