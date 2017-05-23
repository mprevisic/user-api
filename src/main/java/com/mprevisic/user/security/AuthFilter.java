package com.mprevisic.user.security;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mprevisic.user.Constants;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * Spring Security authentication filter. Validates the authentication data
 * (CSRF token and JWT access token).
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class AuthFilter extends OncePerRequestFilter {

	private AuthenticationManager authenticationManager;

	public AuthFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		try {
			// options method doesn't require authentication
			if ("OPTIONS".equals(req.getMethod())) {
				chain.doFilter(request, response);
				return;
			}

			validateCrsfToken(req);

			String jwtTCookie = getJwtCookie(req);

			JWT jwt = JWTParser.parse(jwtTCookie);
			JwtToken jwtToken = new JwtToken(jwt);

			Authentication auth = authenticationManager.authenticate(jwtToken);
			SecurityContextHolder.getContext().setAuthentication(auth);

			chain.doFilter(request, response);
		} catch (AuthenticationException | ParseException e) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} finally {
			// Clear the security context after all filters in chain have
			// executed
			SecurityContextHolder.clearContext();
		}
	}

	private String getJwtCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			throw new InsufficientAuthenticationException("JWT cookie not found");
		}

		Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> Constants.JWT_ACCESS_TOKEN.equals(c.getName()))
				.findFirst();

		if (!cookie.isPresent()) {
			throw new InsufficientAuthenticationException("JWT cookie not found");
		}

		String jwtTCookie = cookie.get().getValue();
		if (jwtTCookie == null) {
			throw new InsufficientAuthenticationException("JWT cookie not found");
		}
		return jwtTCookie;
	}

	private String getCsrfCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			throw new InsufficientAuthenticationException("CSRF cookie not found");
		}

		Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> Constants.CSRF_TOKEN.equals(c.getName()))
				.findFirst();

		if (!cookie.isPresent()) {
			throw new InsufficientAuthenticationException("CSRF cookie not found");
		}

		String csrfCookie = cookie.get().getValue();
		if (csrfCookie == null) {
			throw new InsufficientAuthenticationException("CSRF cookie not found");
		}
		return csrfCookie;
	}

	/**
	 * Validates CSRF token by checking if CSRF token from HTTP header equals to
	 * the token in the CSRF cookie
	 */
	private void validateCrsfToken(HttpServletRequest req) {
		String cookie = getCsrfCookie(req);
		String csrfHeader = req.getHeader(Constants.CSRF_TOKEN);
		if (!cookie.equals(csrfHeader)) {
			throw new BadCredentialsException("Invalid CSRF token");
		}
	}

}
