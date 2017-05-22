package com.mprevisic.user.util;

import java.security.SecureRandom;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

/**
 * Util for dealing with CSRF tokens
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Component
public class CsrfTokenUtil {
	
	private final SecureRandom random = new SecureRandom();
	
	/**
	 * Generates new CSRF token as a base 64 encoded random
	 * byte sequence
	 */
	public String generateToken() {
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		return Base64.toBase64String(bytes);
	}

}
