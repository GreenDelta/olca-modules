package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.util.IlcdConfig;
import org.openlca.ilcd.util.LangString;

public class NetworkPutFlowTest {

	private NetworkClient client;

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
		flow.flowInformation = info;
		info.setDataSetInformation(makeDataInfo(id));
		flow.administrativeInformation = makeAdminInfo();
		ModellingAndValidation mav = new ModellingAndValidation();
		flow.modellingAndValidation = mav;
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = FlowType.ELEMENTARY_FLOW;
		client.put(flow, id);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest Flow - " + new Random().nextInt(1000);
		FlowName fName = new FlowName();
		info.name = fName;
		LangString.addLabel(fName.baseName, name, IlcdConfig.getDefault());
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
