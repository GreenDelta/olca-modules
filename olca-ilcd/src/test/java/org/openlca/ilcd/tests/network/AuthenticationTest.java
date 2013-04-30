package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.io.Authentication;

public class AuthenticationTest {

	@Test
	public void testIsAuthenticated() {
		Authentication auth = getAuthentication("auth_info.xml");
		assertTrue(auth.isAuthenticated());
		assertTrue(auth.getUserName().equals("openlca"));
		assertTrue(auth.isReadAllowed());
		assertTrue(auth.isExportAllowed());
	}

	@Test
	public void testIsNotAuthenticated() {
		Authentication auth = getAuthentication("auth_info_no_auth.xml");
		assertFalse(auth.isAuthenticated());
		assertNull(auth.getUserName());
		assertFalse(auth.isReadAllowed());
		assertFalse(auth.isExportAllowed());
	}

	private Authentication getAuthentication(String res) {
		try (InputStream is = this.getClass().getResourceAsStream(res)) {
			Authentication authentication = JAXB.unmarshal(is,
					Authentication.class);
			return authentication;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
