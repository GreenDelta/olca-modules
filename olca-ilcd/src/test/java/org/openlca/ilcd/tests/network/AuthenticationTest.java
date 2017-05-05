package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.io.AuthInfo;

public class AuthenticationTest {

	@Test
	public void testIsAuthenticated() {
		AuthInfo auth = getAuthentication("auth_info.xml");
		assertTrue(auth.isAuthenticated);
		assertTrue(auth.user.equals("openlca"));
		assertTrue(auth.roles.contains("ADMIN"));
		assertTrue(auth.roles.contains("SUPER_ADMIN"));
		assertTrue(auth.dataStocks.get(0).isReadAllowed());
		assertTrue(auth.dataStocks.get(0).isExportAllowed());
	}

	@Test
	public void testIsNotAuthenticated() {
		AuthInfo auth = getAuthentication("auth_info_no_auth.xml");
		assertFalse(auth.isAuthenticated);
		assertNull(auth.user);
		assertTrue(auth.dataStocks.isEmpty());
	}

	private AuthInfo getAuthentication(String res) {
		try (InputStream is = this.getClass().getResourceAsStream(res)) {
			AuthInfo authentication = JAXB.unmarshal(is,
					AuthInfo.class);
			return authentication;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
