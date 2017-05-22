package com.mprevisic.user.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mprevisic.user.util.CsrfTokenUtil;

public class CsrfTokenUtilTest {
	
	CsrfTokenUtil csrfTokenUtil = new CsrfTokenUtil();

	@Test
	public void testGenerateToken() {
		String token = csrfTokenUtil.generateToken();
		assertNotNull(token);
	}

}
