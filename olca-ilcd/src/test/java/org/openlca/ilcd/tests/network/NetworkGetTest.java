package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

public class NetworkGetTest {

	private String baseUri = "http://localhost:8080/soda4LCA/resource";
	private NetworkClient client = new NetworkClient(baseUri);

	@Test
	public void testCreateWithPassword() {
		NetworkClient client = new NetworkClient(baseUri, "admin", "default");
		assertNotNull(client);
	}

	@Ignore
	@Test(expected = Exception.class)
	public void testCreateWithWrongPassword() {
		new NetworkClient(baseUri, "user", "invalid");
	}

	@Test
	public void testGetProcess() throws Exception {
		String id = "76d6aaa4-37e2-40b2-994c-03292b600074";
		Process process = client.get(Process.class, id);
		assertEquals(id, process.getProcessInformation()
				.getDataSetInformation().getUUID());
		testContains(Process.class, id);
	}

	@Test
	public void testGetFlow() throws Exception {
		String id = "8e1e39c0-11ef-4607-85f0-157ae68f6c63";
		Flow flow = client.get(Flow.class, id);
		assertEquals(id, flow.getFlowInformation().getDataSetInformation()
				.getUUID());
		testContains(Flow.class, id);
	}

	@Test
	public void testGetFlowProperty() throws Exception {
		String id = "93a60a56-a3c8-13da-a746-0800200c9a66";
		FlowProperty property = client.get(FlowProperty.class, id);
		assertEquals(id, property.getFlowPropertyInformation()
				.getDataSetInformation().getUUID());
		testContains(FlowProperty.class, id);
	}

	@Test
	public void testGetUnitGroup() throws Exception {
		String id = "93a60a57-a3c8-12da-a746-0800200c9a66";
		UnitGroup group = client.get(UnitGroup.class, id);
		assertEquals(id, group.getUnitGroupInformation()
				.getDataSetInformation().getUUID());
		testContains(UnitGroup.class, id);
	}

	@Test
	public void testGetContact() throws Exception {
		String id = "d0d5f8bb-9311-49d1-9e30-2f20a6977f4f";
		Contact contact = client.get(Contact.class, id);
		assertEquals(id, contact.getContactInformation()
				.getDataSetInformation().getUUID());
		testContains(Contact.class, id);
	}

	@Test
	public void testGetSource() throws Exception {
		String id = "a97a0155-0234-4b87-b4ce-a45da52f2a40";
		Source source = client.get(Source.class, id);
		assertEquals(id, source.getSourceInformation().getDataSetInformation()
				.getUUID());
		testContains(Source.class, id);
	}

	private void testContains(Class<?> type, String id) throws Exception {
		assertTrue(client.contains(type, id));
		assertFalse(client.contains(type, UUID.randomUUID().toString()));
	}

}
