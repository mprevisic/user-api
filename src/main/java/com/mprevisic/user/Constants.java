package com.mprevisic.user;

/**
 * Application-wide constants
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public interface Constants {

	public static final String TOKEN_ISSUER = "http://www.markoprevisic.com";

	public static final String JWT_ACCESS_TOKEN = "jwt-access-token";

	public static final String JWT_REFRESH_TOKEN = "jwt-refresh-token";

	public static final String CSRF_TOKEN = "xsrf-token";

	public static final long ACCESS_TOKEN_TTL = 60 * 60 * 1000L;

	public static final long REFRESH_TOKEN_TTL = 24 * 60 * 60 * 1000L;

}
