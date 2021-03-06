package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

public class NetworkGetTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!TestServer.isAvailable())
			return;
		DataSets.upload();
		client = TestServer.newClient();
	}

	@Test
	public void testCreateWithPassword() {
		Assume.assumeTrue(TestServer.isAvailable());
		SodaClient client = TestServer.newClient();
		assertNotNull(client);
	}

	@Test(expected = Exception.class)
	public void testCreateWithWrongPassword() {
		Assume.assumeTrue(TestServer.isAvailable());
		SodaConnection con = new SodaConnection();
		con.url = TestServer.ENDPOINT;
		con.user = "user";
		con.password = "invalid";
		SodaClient client = new SodaClient(con);
		client.close();
	}

	@Test
	public void testGetProcess() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "76d6aaa4-37e2-40b2-994c-03292b600074";
		Process process = client.get(Process.class, id);
		assertEquals(id, process.processInfo.dataSetInfo.uuid);
		testContains(Process.class, id);
	}

	@Test
	public void testGetFlow() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "0d7a3ad1-6556-11dd-ad8b-0800200c9a66";
		Flow flow = client.get(Flow.class, id);
		assertEquals(id, flow.flowInfo.dataSetInfo.uuid);
		testContains(Flow.class, id);
	}

	@Test
	public void testGetFlowProperty() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "93a60a56-a3c8-14da-a746-0800200c9a66";
		FlowProperty property = client.get(FlowProperty.class, id);
		assertEquals(id, property.flowPropertyInfo.dataSetInfo.uuid);
		testContains(FlowProperty.class, id);
	}

	@Test
	public void testGetUnitGroup() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "59f191d6-5dd3-4553-af88-1a32accfe308";
		UnitGroup group = client.get(UnitGroup.class, id);
		assertEquals(id, group.unitGroupInfo.dataSetInfo.uuid);
		testContains(UnitGroup.class, id);
	}

	@Test
	public void testGetContact() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "177ca340-ffa2-11da-92e3-0800200c9a66";
		Contact contact = client.get(Contact.class, id);
		assertEquals(id, contact.contactInfo.dataSetInfo.uuid);
		testContains(Contact.class, id);
	}

	@Test
	public void testGetSource() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = "2c699413-f88b-4cb5-a56d-98cb4068472f";
		Source source = client.get(Source.class, id);
		assertEquals(id, source.sourceInfo.dataSetInfo.uuid);
		testContains(Source.class, id);
	}

	private void testContains(Class<? extends IDataSet> type, String id) {
		assertTrue(client.contains(type, id));
		assertFalse(client.contains(type, UUID.randomUUID().toString()));
	}

}
