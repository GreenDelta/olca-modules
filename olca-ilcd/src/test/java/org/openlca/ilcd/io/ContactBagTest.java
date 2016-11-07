package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.util.ContactBag;

public class ContactBagTest {

	private ContactBag bag;

	@Before
	public void setUp() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"contact.xml")) {
			XmlBinder binder = new XmlBinder();
			Contact contact = binder.fromStream(Contact.class, stream);
			bag = new ContactBag(contact, "en");
		}
	}

	@Test
	public void testGetId() {
		assertEquals("177ca340-ffa2-11da-92e3-0800200c9a66", bag.getId());
	}

	@Test
	public void testGetShortName() {
		assertEquals("IISI Review panel 2000", bag.getShortName());
	}

	@Test
	public void testGetName() {
		assertEquals("B. P. Weidema, A. Inaba, G. A. Keoleian", bag.getName());
	}

	@Test
	public void testGetSortedClasses() {
		List<Category> classes = bag.getSortedClasses();
		assertTrue(classes.size() == 1);
		assertEquals("Working groups within organisation", classes.get(0).value);
	}

	@Test
	public void testGetContactAddress() {
		assertEquals("Rue Colonel Bourg 120 B-1140 Brussels, Belgium",
				bag.getContactAddress());
	}

	@Test
	public void testGetTelephone() {
		assertEquals("+32 2 702 89 00", bag.getTelephone());
	}

	@Test
	public void testGetTelefax() {
		assertEquals("+32 2 702 88 99", bag.getTelefax());
	}

	@Test
	public void testGetWebSite() {
		assertEquals("www.worldsteel.org", bag.getWebSite());
	}

	@Test
	public void testGetCentralContactPoint() {
		assertEquals("Rue Colonel Bourg 120 B-1140 Brussels, Belgium",
				bag.getCentralContactPoint());
	}

}
