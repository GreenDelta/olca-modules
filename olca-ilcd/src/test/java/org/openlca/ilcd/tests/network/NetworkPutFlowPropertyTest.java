package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.Publication;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.util.IlcdConfig;
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
		FlowPropertyInfo info = new FlowPropertyInfo();
		flowproperty.flowPropertyInformation = info;
		info.dataSetInformation = makeDataInfo(id);
		flowproperty.administrativeInformation = makeAdminInfo();
		client.put(flowproperty, id);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest FlowProperty - " + new Random().nextInt(1000);
		LangString.addLabel(info.name, name, IlcdConfig.getDefault());
		info.uuid = id;
		return info;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publication = pub;
		pub.dataSetVersion = "01.00.000";
		return info;
	}
}