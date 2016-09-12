package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;
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
		ContactInfo info = new ContactInfo();
		contact.contactInformation = info;
		info.setDataSetInformation(makeDataInfo(id));
		contact.administrativeInformation = makeAdminInfo();
		client.put(contact, id);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo dataSetInfo = new DataSetInfo();
		String name = "xtest contact - " + new Random().nextInt(1000);
		LangString.addLabel(dataSetInfo.name, name,
				IlcdConfig.getDefault());
		LangString.addLabel(dataSetInfo.shortName, name,
				IlcdConfig.getDefault());
		dataSetInfo.uuid = id;
		return dataSetInfo;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publication = pub;
		pub.dataSetVersion = "01.00.000";
		return info;
	}
}
