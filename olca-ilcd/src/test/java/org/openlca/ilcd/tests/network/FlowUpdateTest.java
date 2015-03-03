package org.openlca.ilcd.tests.network;

import java.util.UUID;

import org.junit.Assert;
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
import org.openlca.ilcd.util.LangString;

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
		flow.getAdministrativeInformation()
				.getPublication()
				.setDataSetVersion("02.00.000");
		client.put(flow, id);
		flow = client.get(Flow.class, id);
		Assert.assertEquals("02.00.000", flow.getAdministrativeInformation()
				.getPublication().getDataSetVersion());
	}

	private Flow makeFlow(String id) {
		Flow flow = new Flow();
		FlowInformation info = new FlowInformation();
		flow.setFlowInformation(info);
		DataSetInformation dataInfo = new DataSetInformation();
		dataInfo.setUUID(id);
		info.setDataSetInformation(dataInfo);
		FlowName name = new FlowName();
		dataInfo.setName(name);
		LangString.addLabel(name.getBaseName(), "test flow - " + id);
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		Publication pub = new Publication();
		adminInfo.setPublication(pub);
		pub.setDataSetVersion("01.00.000");
		flow.setAdministrativeInformation(adminInfo);
		ModellingAndValidation mav = new ModellingAndValidation();
		flow.setModellingAndValidation(mav);
		LCIMethod method = new LCIMethod();
		mav.setLCIMethod(method);
		method.setFlowType(FlowType.ELEMENTARY_FLOW);
		return flow;
	}
}
