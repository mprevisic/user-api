package com.mprevisic.user.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.mprevisic.user.util.UserBlacklistCache;

/**
 * Spring Security configuration
 * 
 * @author Marko Previsic
 * @created May 22, 2017
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private KeyPairContainer keyPairContainer;

	@Autowired
	private UserBlacklistCache deletedUserCache;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder authManager) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
	    return super.authenticationManagerBean();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// disable spring default CSRF protection because of 
		// using an own custom mechanism
		http.csrf().disable();

		List<AuthenticationProvider> authManagers = new ArrayList<AuthenticationProvider>();
		authManagers.add(new JwtAuthProvider(keyPairContainer, deletedUserCache));
		ProviderManager providerManager = new ProviderManager(authManagers);

		// everyone can access session management API and user registration
		// method
		http.authorizeRequests().antMatchers("/api/v1/session").permitAll().and().authorizeRequests()
				.antMatchers("/api/v1/token").permitAll().and().authorizeRequests()
				.antMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/v1/users/?*").permitAll();;

		// only authenticated users can access user management API
		http.antMatcher("/api/v1/users/?*")
				.addFilterBefore(new AuthFilter(providerManager), BasicAuthenticationFilter.class).authorizeRequests()
				.anyRequest().authenticated();
	}

}