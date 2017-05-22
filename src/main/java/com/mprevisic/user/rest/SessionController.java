package com.mprevisic.user.rest;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mprevisic.user.Constants;
import com.mprevisic.user.Credentials;
import com.mprevisic.user.dto.UserDto;
import com.mprevisic.user.service.UserService;
import com.mprevisic.user.util.CsrfTokenUtil;
import com.mprevisic.user.util.JwtUtil;

/**
 * REST API endpoint of the session management API
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@RestController
@RequestMapping("/api/v1")
public class SessionController {

	@Autowired
	private UserService userService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private JwtUtil JwtUtil;

	@Autowired
	private CsrfTokenUtil csrfUtil;

	@Value("server.ssl.key-store")
	private String sslKeyStore;
	
	@RequestMapping(value = "/session", method = RequestMethod.OPTIONS)
	public ResponseEntity<Object> getSessionOptions(HttpServletResponse response) {
	    response.setHeader("Allow", "OPTIONS,POST");
	    return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Authenticates the user. If successful, returns HTTP status 200 together
	 * with the following cookies: - JWT refresh token - JWT access token -
	 * Anti-CSRF token
	 * 
	 * Returns HTTP status 401 (Unauthorized) if authentication failed.
	 */
	@RequestMapping(path = "/session", consumes = "application/json", method = RequestMethod.POST)
	public ResponseEntity<Object> login(@RequestBody Credentials credentials) {
		boolean authenticated = userService.authenticate(credentials);

		if (!authenticated) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		HttpHeaders headers = setCookies(credentials.getEmail(), true);

		return new ResponseEntity<>(headers, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/token", method = RequestMethod.OPTIONS)
	public ResponseEntity<Object> getTokenOptions(HttpServletResponse response) {
	    response.setHeader("Allow", "OPTIONS,POST");
	    return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Refreshes the JWT access token. Expects a valid refresh token provided
	 * through a cookie.
	 */
	@RequestMapping(path = "/token", consumes = "application/json", method = RequestMethod.POST)
	public ResponseEntity<Object> refreshToken() {
		String refToken = getRefreshToken();

		Optional<String> user = JwtUtil.validateRefreshToken(refToken);

		if (user.isPresent()) {
			HttpHeaders headers = setCookies(user.get(), false);

			return new ResponseEntity<>(headers, HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}

	private HttpHeaders setCookies(String user, boolean setRefreshToken) {
		Optional<UserDto> userDto = userService.findByEmail(user);

		String accessToken = JwtUtil.createAccessToken(user, userDto.get().getRole());
		String csrfToken = csrfUtil.generateToken();

		// access token duration is 1 hour
		long accessTokenMaxAge = Constants.ACCESS_TOKEN_TTL / 1000L;

		String accessTokenCookie = Constants.JWT_ACCESS_TOKEN + "=" + accessToken + "; HttpOnly; max-age="
				+ accessTokenMaxAge + ";";

		// set 'secure' attribute only if HTTPS is used
		if (httpsEnabled()) {
			accessTokenCookie += "secure; ";
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Set-Cookie", accessTokenCookie);
		headers.add("Set-Cookie", Constants.CSRF_TOKEN + "=" + csrfToken);

		if (setRefreshToken) {
			String refreshToken = JwtUtil.createRefreshToken(user, userDto.get().getRole());
			long refTokenMaxAge = Constants.REFRESH_TOKEN_TTL / 1000L;

			String refreshTokenCookie = Constants.JWT_REFRESH_TOKEN + "=" + refreshToken + "; HttpOnly; max-age="
					+ refTokenMaxAge + ";";

			if (httpsEnabled()) {
				refreshTokenCookie += "secure; ";
			}

			headers.add("Set-Cookie", refreshTokenCookie);
		}

		return headers;
	}

	/**
	 * Reads JWT refresh token from cookies
	 * 
	 * @return JWT refresh token
	 */
	private String getRefreshToken() {
		Cookie[] cookies = request.getCookies();

		Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> Constants.JWT_REFRESH_TOKEN.equals(c.getName()))
				.findFirst();

		if (cookie.isPresent()) {
			return cookie.get().getValue();
		}

		return null;
	}

	/**
	 * Checks if HTTPS enabled by checking SSL config from
	 * application.properties
	 */
	private boolean httpsEnabled() {
		return sslKeyStore != null;
	}

}
