package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.contacts.AdministrativeInformation;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInformation;
import org.openlca.ilcd.contacts.DataSetInformation;
import org.openlca.ilcd.contacts.Publication;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.util.IlcdConfig;
import org.openlca.ilcd.util.LangString;

public class NetworkPutContactTest {

	private NetworkClient client;

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
		ContactInformation info = new ContactInformation();
		contact.setContactInformation(info);
		info.setDataSetInformation(makeDataInfo(id));
		contact.setAdministrativeInformation(makeAdminInfo());
		client.put(contact, id);
	}

	private DataSetInformation makeDataInfo(String id) {
		DataSetInformation dataSetInfo = new DataSetInformation();
		String name = "xtest contact - " + new Random().nextInt(1000);
		LangString.addLabel(dataSetInfo.getName(), name,
				IlcdConfig.getDefault());
		LangString.addLabel(dataSetInfo.getShortName(), name,
				IlcdConfig.getDefault());
		dataSetInfo.setUUID(id);
		return dataSetInfo;
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		Publication pub = new Publication();
		info.setPublication(pub);
		pub.setDataSetVersion("01.00.000");
		return info;
	}
}
