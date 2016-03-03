package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdministrativeInformation;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInformation;
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
		FlowInformation info = new FlowInformation();
		flow.setFlowInformation(info);
		info.setDataSetInformation(makeDataInfo(id));
		flow.setAdministrativeInformation(makeAdminInfo());
		ModellingAndValidation mav = new ModellingAndValidation();
		flow.setModellingAndValidation(mav);
		LCIMethod method = new LCIMethod();
		mav.setLCIMethod(method);
		method.setFlowType(FlowType.ELEMENTARY_FLOW);
		client.put(flow, id);
	}

	private DataSetInformation makeDataInfo(String id) {
		DataSetInformation info = new DataSetInformation();
		String name = "xtest Flow - " + new Random().nextInt(1000);
		FlowName fName = new FlowName();
		info.setName(fName);
		LangString.addLabel(fName.getBaseName(), name, IlcdConfig.getDefault());
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
