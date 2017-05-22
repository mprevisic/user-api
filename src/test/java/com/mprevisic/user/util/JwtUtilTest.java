package com.mprevisic.user.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.mprevisic.user.Constants;
import com.mprevisic.user.domain.UserEntity;
import com.mprevisic.user.repository.UserRepository;
import com.mprevisic.user.security.KeyPairContainer;
import com.mprevisic.user.util.JwtUtil;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

public class JwtUtilTest {

	private JwtUtil jwtUtil;

	@Before
	public void setUp() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(1024);
		KeyPair kp = keyGenerator.genKeyPair();

		KeyPairContainer keyPairContainer = mock(KeyPairContainer.class);
		when(keyPairContainer.getPrivateKey()).thenReturn((RSAPrivateKey) kp.getPrivate());
		when(keyPairContainer.getPublicKey()).thenReturn((RSAPublicKey) kp.getPublic());

		UserEntity user = new UserEntity();
		user.setEmail("user@gmail.com");
		user.setPassword("xyz");
		user.setRole(1);

		UserRepository userRepo = mock(UserRepository.class);
		when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
		when(userRepo.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

		jwtUtil = new JwtUtil(keyPairContainer, userRepo);
	}

	@Test
	public void testCreateAccessToken() throws ParseException {
		String jwtToken = jwtUtil.createAccessToken("user@gmail.com", 1);
		JWT jwt = JWTParser.parse(jwtToken);

		ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
		String issuer = claims.getIssuer();

		assertEquals(Constants.TOKEN_ISSUER, issuer);

		Date expirationTime = claims.getExpirationTime();

		assertTrue(expirationTime.after(new Date()));
	}

	@Test
	public void testCreateRefreshToken() throws ParseException {
		String jwtToken = jwtUtil.createRefreshToken("user@gmail.com", 1);
		JWT jwt = JWTParser.parse(jwtToken);

		ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
		String issuer = claims.getIssuer();

		assertEquals(Constants.TOKEN_ISSUER, issuer);

		Date expirationTime = claims.getExpirationTime();

		assertTrue(expirationTime.after(new Date()));
	}

	@Test
	public void testValidateRefreshToken() {
		String jwtToken = jwtUtil.createRefreshToken("user@gmail.com", 1);

		Optional<String> user = jwtUtil.validateRefreshToken(jwtToken);

		assertTrue(user.isPresent());

		assertEquals("user@gmail.com", user.get());
	}

	@Test
	public void validateRefreshTokenUserNotFound() {
		String jwtToken = jwtUtil.createRefreshToken("xyz@gmail.com", 1);

		Optional<String> user = jwtUtil.validateRefreshToken(jwtToken);

		assertFalse(user.isPresent());
	}

}
