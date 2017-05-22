package com.mprevisic.user.security;

import java.text.ParseException;
import java.util.Date;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.mprevisic.user.Constants;
import com.mprevisic.user.util.UserBlacklistCache;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Custom authentication provider which checks
 * the content of the JWT access token
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class JwtAuthProvider implements AuthenticationProvider {
    
    private JWSVerifier verifier;
    
    private UserBlacklistCache deletedUserCache;
    
    public JwtAuthProvider(KeyPairContainer keyPairContainer, UserBlacklistCache deletedUserCache) {
        this.verifier = new RSASSAVerifier(keyPairContainer.getPublicKey());
        this.deletedUserCache = deletedUserCache;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authentication;
        JWT jwt = jwtToken.getJwt();
        
        // Check type of the parsed JOSE object
        if (jwt instanceof PlainJWT) {
            handlePlainToken((PlainJWT) jwt);
        } else if (jwt instanceof SignedJWT) {
            handleSignedToken((SignedJWT) jwt);
        } else if (jwt instanceof EncryptedJWT) {
            handleEncryptedToken((EncryptedJWT) jwt);
        }
        
        Date referenceTime = new Date();
        ReadOnlyJWTClaimsSet claims = null;
		try {
			claims = jwtToken.getJwt().getJWTClaimsSet();
		} catch (ParseException e) {
			throw new BadCredentialsException("JWT access token could not be parsed");
		}
        
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(referenceTime)) {
        	throw new BadCredentialsException("JWT access token is expired");
        }
        
        String issuerReference = Constants.TOKEN_ISSUER;
        String issuer = claims.getIssuer();
        if (!issuerReference.equals(issuer)) {
        	throw new BadCredentialsException("Invalid JWT access token issuer");
        }
        
        String user = claims.getSubject();
        if (deletedUserCache.checkUserDeleted(user)) {
        	throw new BadCredentialsException("User not found");
        }
        
        jwtToken.setAuthenticated(true);
        return jwtToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtToken.class.isAssignableFrom(authentication);
    }
    
    private void handlePlainToken(PlainJWT jwt) {
        throw new BadCredentialsException("Unsecured plain tokens are not supported");
    }
    
    private void handleSignedToken(SignedJWT jwt) {
        try {
            if (!jwt.verify(verifier)) {
                throw new BadCredentialsException("Signature validation failed");
            }
        } catch (JOSEException e) {
            throw new BadCredentialsException("Signature validation failed");
        }
    }
    
    private void handleEncryptedToken(EncryptedJWT jwt) {
        throw new BadCredentialsException("Unsupported token type");
    }
    
}