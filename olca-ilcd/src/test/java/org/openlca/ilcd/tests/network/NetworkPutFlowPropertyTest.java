package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.flowproperties.AdministrativeInformation;
import org.openlca.ilcd.flowproperties.DataSetInformation;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInformation;
import org.openlca.ilcd.flowproperties.Publication;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.util.LangString;

public class NetworkPutFlowPropertyTest {

	private NetworkClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutFlowProperty() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		FlowProperty flowproperty = new FlowProperty();
		FlowPropertyInformation info = new FlowPropertyInformation();
		flowproperty.setFlowPropertyInformation(info);
		info.setDataSetInformation(makeDataInfo(id));
		flowproperty.setAdministrativeInformation(makeAdminInfo());
		client.put(flowproperty, id);
	}

	private DataSetInformation makeDataInfo(String id) {
		DataSetInformation info = new DataSetInformation();
		String name = "xtest FlowProperty - " + new Random().nextInt(1000);
		LangString.addLabel(info.getName(), name);
		info.setUUID(id);
		return info;
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		Publication pub = new Publication();
		info.setPublication(pub);
		pub.setDataSetVersion("01.00.000");
		return info;
	}
}