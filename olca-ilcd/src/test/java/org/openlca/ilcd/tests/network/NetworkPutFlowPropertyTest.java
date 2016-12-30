package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.io.SodaClient;

public class NetworkPutFlowPropertyTest {

	private SodaClient client;

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
		FlowProperty fp = new FlowProperty();
		FlowPropertyInfo info = new FlowPropertyInfo();
		fp.flowPropertyInfo = info;
		info.dataSetInfo = makeDataInfo(id);
		fp.adminInfo = makeAdminInfo();
		client.put(fp);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest FlowProperty - " + new Random().nextInt(1000);
		LangString.set(info.name, name, "en");
		info.uuid = id;
		return info;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = "01.00.000";
		return info;
	}
}