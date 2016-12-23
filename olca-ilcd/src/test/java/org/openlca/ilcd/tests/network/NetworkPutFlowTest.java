package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.io.SodaClient;

public class NetworkPutFlowTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutFlow() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		Flow flow = new Flow();
		FlowInfo info = new FlowInfo();
		flow.flowInfo = info;
		info.dataSetInfo = makeDataInfo(id);
		flow.adminInfo = makeAdminInfo();
		Modelling mav = new Modelling();
		flow.modelling = mav;
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = FlowType.ELEMENTARY_FLOW;
		client.put(flow);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest Flow - " + new Random().nextInt(1000);
		FlowName fName = new FlowName();
		info.name = fName;
		LangString.set(fName.baseName, name, "en");
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
