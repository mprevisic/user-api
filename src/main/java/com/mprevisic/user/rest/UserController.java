package com.mprevisic.user.rest;

import java.util.Map;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mprevisic.user.dto.UserDto;
import com.mprevisic.user.service.UserService;

/**
 * REST API endpoint of the user management API.
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass()); 

	private static final String EMAIL_REGEX = "^(.+)@(.+)$";

	@Autowired
	private UserService userService;
	
	@RequestMapping(method = RequestMethod.OPTIONS)
	public ResponseEntity<Object> getOptions(HttpServletResponse response) {
	    response.setHeader("Allow", "OPTIONS,POST");
	    return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(path = "/{userId}", method = RequestMethod.OPTIONS)
	public ResponseEntity<Object> getUserOptions(HttpServletResponse response) {
	    response.setHeader("Allow", "OPTIONS,GET,PATCH,DELETE");
	    return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Registers a new user
	 */
	@RequestMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
	public ResponseEntity<Object> register(@RequestBody final UserDto user) {
		if (user.getEmail() == null || user.getEmail().isEmpty()) {
			return new ResponseEntity<>(new Message("E-mail address is missing!"), HttpStatus.BAD_REQUEST);
		}

		if (!user.getEmail().matches(EMAIL_REGEX)) {
			return new ResponseEntity<>(new Message("E-mail address has invalid format!"), HttpStatus.BAD_REQUEST);
		}

		if (user.getPassword() == null || user.getPassword().isEmpty()) {
			return new ResponseEntity<>(new Message("Password is missing!"), HttpStatus.BAD_REQUEST);
		}

		Optional<UserDto> existingUser = userService.findByEmail(user.getEmail());

		if (existingUser.isPresent()) {
			return new ResponseEntity<>(new Message("User with e-mail '" + user.getEmail() + " already exists!"),
					HttpStatus.BAD_REQUEST);
		}

		LOG.debug("Creating new user [e-mail='" + user.getEmail() + ']');
		
		UserDto savedUser = userService.saveUser(user);

		return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
	}

	/**
	 * Returns user with given ID
	 */
	@RequestMapping(path = "/{userId}", produces = "application/json", method = RequestMethod.GET)
	public ResponseEntity<Object> getUser(@PathVariable("userId") final Long userId) {
		Optional<UserDto> user = userService.findById(userId);

		if (!user.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(user.get(), HttpStatus.OK);
	}

	/**
	 * Updates a user
	 */
	@RequestMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PATCH)
	public ResponseEntity<Object> updateUser(@PathVariable("userId") final Long userId,
			@RequestBody final Map<String, Object> newData) {
		Optional<UserDto> user = userService.findById(userId);

		if (!user.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		LOG.debug("Updating user " + user.get().getEmail());
		UserDto savedUser = userService.updateUser(user.get(), newData);

		return new ResponseEntity<>(savedUser, HttpStatus.OK);
	}
	
	/**
	 * Deletes user with given ID
	 */
	@RequestMapping(path = "/{userId}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteUser(@PathVariable("userId") final Long userId) {
		Optional<UserDto> user = userService.findById(userId);

		if (!user.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		LOG.debug("Deleting user " + user.get().getEmail());
		
		userService.deleteUser(userId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
