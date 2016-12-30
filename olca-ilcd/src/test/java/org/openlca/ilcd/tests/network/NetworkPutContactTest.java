package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;
import org.openlca.ilcd.io.SodaClient;

public class NetworkPutContactTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutContact() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		Contact contact = new Contact();
		ContactInfo info = new ContactInfo();
		contact.contactInfo = info;
		info.dataSetInfo = makeDataInfo(id);
		contact.adminInfo = makeAdminInfo();
		client.put(contact);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo dataSetInfo = new DataSetInfo();
		String name = "xtest contact - " + new Random().nextInt(1000);
		LangString.set(dataSetInfo.name, name, "en");
		LangString.set(dataSetInfo.shortName, name, "en");
		dataSetInfo.uuid = id;
		return dataSetInfo;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = "01.00.000";
		return info;
	}
}
