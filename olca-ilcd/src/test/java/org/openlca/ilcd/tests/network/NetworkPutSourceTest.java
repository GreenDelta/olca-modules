package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;

public class NetworkPutSourceTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutSource() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		Source source = new Source();
		SourceInfo info = new SourceInfo();
		source.sourceInfo = info;
		info.dataSetInfo = makeDataInfo(id);
		source.adminInfo = makeAdminInfo();
		client.put(source);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest Source - " + new Random().nextInt(1000);
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