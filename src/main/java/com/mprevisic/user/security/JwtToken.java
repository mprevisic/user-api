package com.mprevisic.user.security;

import java.text.ParseException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

/**
 * JWT access token authentication object
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
public class JwtToken implements Authentication {

	private static final long serialVersionUID = 1L;

	private boolean authenticated;

	private ReadOnlyJWTClaimsSet claims;

	private JWT jwt;

	public JwtToken(JWT jwt) throws ParseException {
		claims = jwt.getJWTClaimsSet();
		this.jwt = jwt;
	}

	@Override
	public String getName() {
		return claims.getSubject();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.commaSeparatedStringToAuthorityList(
				(String) claims.getCustomClaim("permissions"));
	}

	@Override
	public Object getCredentials() {
		return "";
	}

	@Override
	public Object getDetails() {
		return claims.toJSONObject();
	}

	@Override
	public Object getPrincipal() {
		return claims.getSubject();
	}

	public JWT getJwt() {
		return jwt;
	}

	public void setJwt(JWT jwt) {
		this.jwt = jwt;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public void setAuthenticated(boolean arg0) throws IllegalArgumentException {
		this.authenticated = arg0;
	}

}
