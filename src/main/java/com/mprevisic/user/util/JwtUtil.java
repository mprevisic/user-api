package com.mprevisic.user.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mprevisic.user.Constants;
import com.mprevisic.user.repository.UserRepository;
import com.mprevisic.user.security.KeyPairContainer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Utility for dealing with JWT tokens
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Component
public class JwtUtil {

	private final JWSSigner signer;

	private final JWSVerifier verifier;
	
	private final UserRepository userRepo;
	

	@Autowired
	public JwtUtil(KeyPairContainer keyPairContainer, UserRepository userRepo) {
		// Create RSA-signer with the private key
		signer = new RSASSASigner(keyPairContainer.getPrivateKey());
		verifier = new RSASSAVerifier(keyPairContainer.getPublicKey());
		this.userRepo = userRepo;
	}

	/**
	 * Creates JWT access token for given user
	 */
	public String createAccessToken(String username, Integer role) {
		return createToken(username, role, Constants.ACCESS_TOKEN_TTL);
	}

	/**
	 * Creates JWT refresh token for given user
	 */
	public String createRefreshToken(String username, Integer role) {
		return createToken(username, role, Constants.REFRESH_TOKEN_TTL);
	}
	
	private String createToken(String username, Integer role, long ttl) {
		// Prepare JWT with claims set
		JWTClaimsSet claimsSet = new JWTClaimsSet();
		claimsSet.setSubject(username);
		claimsSet.setIssuer(Constants.TOKEN_ISSUER);
		claimsSet.setExpirationTime(new Date(new Date().getTime() + ttl));
		claimsSet.setCustomClaim("role", role);

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

		// Compute the RSA signature
		try {
			signedJWT.sign(signer);
		} catch (JOSEException e) {
			e.printStackTrace();
		}

		return signedJWT.serialize();
	}

	/**
	 * Validates the JWT refresh token and returns the user holding the
	 * token if valid
	 */
	public Optional<String> validateRefreshToken(String refreshToken) {
		String user = null;
		
		try {
			JWT jwt = JWTParser.parse(refreshToken);

			if (jwt instanceof SignedJWT) {
				SignedJWT signedJwt = (SignedJWT) jwt;

				if (!signedJwt.verify(verifier)) {
					return Optional.empty();
				}

				if (checkTokenExpired(jwt)) {
					return Optional.empty();
				}
				
				if (checkUserExists(jwt)) {
					user = jwt.getJWTClaimsSet().getSubject();
				} else {
					return Optional.empty();
				}
				
				if (!checkIssuer(jwt)) {
					return Optional.empty();
				}
			}
		} catch (ParseException | JOSEException ex) {
			return Optional.empty();
		}

		return Optional.of(user);
	}

	private boolean checkTokenExpired(JWT jwt) throws ParseException {
		Date referenceTime = new Date();
		ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();

		Date expirationTime = claims.getExpirationTime();
		if (expirationTime == null || expirationTime.before(referenceTime)) {
			return true;
		}

		return false;
	}

	private boolean checkIssuer(JWT jwt) throws ParseException {
		ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
		String issuer = claims.getIssuer();
		if (!Constants.TOKEN_ISSUER.equals(issuer)) {
			return false;
		}
		return true;
	}

	private boolean checkUserExists(JWT jwt) throws ParseException {
		ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
		String email = claims.getSubject();
		
		if (userRepo.findByEmail(email).isPresent()) {
			return true;
		}
		
		return false;
	}

}
