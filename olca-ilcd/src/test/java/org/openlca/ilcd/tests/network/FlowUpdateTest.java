package org.openlca.ilcd.tests.network;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.io.NetworkClient;

public class FlowUpdateTest {

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
		Flow flow = makeFlow(id);
		client.put(flow, id);
		Assert.assertTrue(client.contains(Flow.class, id));
		flow.administrativeInformation.publication.dataSetVersion = "02.00.000";
		client.put(flow, id);
		flow = client.get(Flow.class, id);
		Assert.assertEquals("02.00.000", flow.administrativeInformation.publication.dataSetVersion);
	}

	private Flow makeFlow(String id) {
		Flow flow = new Flow();
		FlowInfo info = new FlowInfo();
		flow.flowInformation = info;
		DataSetInfo dataInfo = new DataSetInfo();
		dataInfo.uuid = id;
		info.dataSetInfo = dataInfo;
		FlowName name = new FlowName();
		dataInfo.name = name;
		LangString.set(name.baseName, "test flow - " + id, "en");
		AdminInfo adminInfo = new AdminInfo();
		Publication pub = new Publication();
		adminInfo.publication = pub;
		pub.dataSetVersion = "01.00.000";
		flow.administrativeInformation = adminInfo;
		ModellingAndValidation mav = new ModellingAndValidation();
		flow.modellingAndValidation = mav;
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = FlowType.ELEMENTARY_FLOW;
		return flow;
	}
}
