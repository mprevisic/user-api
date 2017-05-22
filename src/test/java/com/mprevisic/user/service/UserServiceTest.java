package com.mprevisic.user.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mprevisic.user.Credentials;
import com.mprevisic.user.domain.UserEntity;
import com.mprevisic.user.dto.UserDto;
import com.mprevisic.user.repository.UserRepository;
import com.mprevisic.user.service.UserService;
import com.mprevisic.user.util.UserBlacklistCache;

public class UserServiceTest {
	
	private UserService userService;
	
	@Before
	public void setUp() {
		UserRepository userRepo = mock(UserRepository.class);
		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String password = passwordEncoder.encode("test123!");
		
		UserEntity user = new UserEntity();
		user.setEmail("user@gmail.com");
		user.setPassword(password);
		user.setRole(1);
		user.setId(2L);
		
		UserEntity user2 = new UserEntity();
		user2.setEmail("user2@gmail.com");
		user2.setPassword(password);
		user2.setRole(1);
		user2.setId(3L);
		
		when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userRepo.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
		when(userRepo.findByEmail("user2@gmail.com")).thenReturn(Optional.of(user2));
		when(userRepo.findOne(anyLong())).thenReturn(null);
		when(userRepo.findOne(2L)).thenReturn(user);
		when(userRepo.save(any(UserEntity.class))).thenReturn(user);
		
		UserBlacklistCache delUserCache = mock(UserBlacklistCache.class);
		
		this.userService = new UserService(userRepo, delUserCache);
	}

	@Test
	public void testAuthenticateUser() {
		Credentials cred = new Credentials();
		cred.setEmail("user@gmail.com");
		cred.setPassword("test123!");
		
		boolean result = userService.authenticate(cred);
		assertTrue(result);
	}

	@Test
	public void authenticateUserNotFound() {
		Credentials cred = new Credentials();
		cred.setEmail("xyz@gmail.com");
		cred.setPassword("test123!");
		
		boolean result = userService.authenticate(cred);
		assertFalse(result);
	}
	
	@Test
	public void authenticateBadPassword() {
		Credentials cred = new Credentials();
		cred.setEmail("xyz@gmail.com");
		cred.setPassword("abc123");
		
		boolean result = userService.authenticate(cred);
		assertFalse(result);
	}
	
	@Test
	public void testFindByEmail() {
		Optional<UserDto> user = userService.findByEmail("user@gmail.com");
		
		assertTrue(user.isPresent());
	}
	
	@Test
	public void findByEmailUserNotFound() {
		Optional<UserDto> user = userService.findByEmail("xyz@gmail.com");
		
		assertFalse(user.isPresent());
	}
	
	@Test
	public void testFindById() {
		Optional<UserDto> user = userService.findById(2L);
		
		assertTrue(user.isPresent());
	}
	
	@Test
	public void findByIdUserNotFound() {
		Optional<UserDto> user = userService.findById(3L);
		
		assertFalse(user.isPresent());
	}
	
	@Test
	public void testSaveUser() {
		UserDto user = createUser();
		
		user = userService.saveUser(user);
		
		assertNotNull(user);
	}
	
	public void saveUserNoPassword() {
		UserDto user = createUser();
		user.setPassword(null);
		
		user = userService.saveUser(user);
		
		assertNotNull(user);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void saveUserEmailAlreadyExists() {
		UserDto user = createUser();
		user.setEmail("user@gmail.com");
		
		userService.saveUser(user);
	}
	
	@Test
	public void testUpdateUser() {
		Optional<UserDto> user = userService.findByEmail("user@gmail.com");
		
		Map<String, Object> updatedUser = new HashMap<>();
		updatedUser.put("role", 13);
		
		UserDto res = userService.updateUser(user.get(), updatedUser);
		
		assertNotNull(res);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void udateUserEmailAlreadyExists() {
		Optional<UserDto> user = userService.findByEmail("user@gmail.com");
		
		Map<String, Object> updatedUser = new HashMap<>();
		updatedUser.put("email", "user2@gmail.com");
		
		userService.updateUser(user.get(), updatedUser);
	}
	
	private UserDto createUser() {
		UserDto user = new UserDto();
		
		user.setEmail("abc@gmail.com");
		user.setPassword("abc123");
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setTitle("Mr.");
		user.setRole(1);
		
		return user;
	}

}
