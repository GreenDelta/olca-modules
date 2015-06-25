package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.sources.AdministrativeInformation;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.Publication;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInformation;
import org.openlca.ilcd.util.LangString;

public class NetworkPutSourceTest {

	private NetworkClient client;

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
		SourceInformation info = new SourceInformation();
		source.setSourceInformation(info);
		info.setDataSetInformation(makeDataInfo(id));
		source.setAdministrativeInformation(makeAdminInfo());
		client.put(source, id);
	}

	private DataSetInformation makeDataInfo(String id) {
		DataSetInformation info = new DataSetInformation();
		String name = "xtest Source - " + new Random().nextInt(1000);
		LangString.addLabel(info.getShortName(), name);
		info.setUUID(id);
		return info;
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		Publication pub = new Publication();
		info.setPublicationAndOwnership(pub);
		pub.setDataSetVersion("01.00.000");
		return info;
	}
}