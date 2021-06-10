package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;
import org.openlca.ilcd.io.AuthInfo;

import jakarta.xml.bind.JAXB;

public class AuthenticationTest {

	@Test
	public void testReadAuthInfo() {
		AuthInfo auth = readTestXml("auth_info.xml");
		assertTrue(auth.isAuthenticated);
		assertEquals("openlca", auth.user);
		assertTrue(auth.roles.contains("ADMIN"));
		assertTrue(auth.roles.contains("SUPER_ADMIN"));
		assertTrue(auth.dataStocks.get(0).isReadAllowed());
		assertTrue(auth.dataStocks.get(0).isExportAllowed());
	}

	@Test
	public void testIsNotAuthenticated() {
		AuthInfo auth = readTestXml("auth_info_no_auth.xml");
		assertFalse(auth.isAuthenticated);
		assertNull(auth.user);
		assertTrue(auth.dataStocks.isEmpty());
	}

	@Test
	public void testReadFromServer() {
		Assume.assumeTrue(TestServer.isAvailable());
		try (var client = TestServer.newClient()) {
			var info = client.getAuthInfo();
			assertTrue(info.isAuthenticated);
			assertEquals(TestServer.USER, info.user);
		}
	}

	private AuthInfo readTestXml(String res) {
		try (var stream = this.getClass().getResourceAsStream(res)) {
			assertNotNull(stream);
			return JAXB.unmarshal(stream, AuthInfo.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
