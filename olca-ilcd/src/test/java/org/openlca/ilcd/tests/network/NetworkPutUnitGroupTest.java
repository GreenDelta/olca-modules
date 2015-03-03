package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.units.AdministrativeInformation;
import org.openlca.ilcd.units.DataSetInformation;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInformation;
import org.openlca.ilcd.util.LangString;

public class NetworkPutUnitGroupTest {

	private NetworkClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testPutUnitGroup() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		UnitGroup unitgroup = new UnitGroup();
		UnitGroupInformation info = new UnitGroupInformation();
		unitgroup.setUnitGroupInformation(info);
		info.setDataSetInformation(makeDataInfo(id));
		unitgroup.setAdministrativeInformation(makeAdminInfo());
		client.put(unitgroup, id);
	}

	private DataSetInformation makeDataInfo(String id) {
		DataSetInformation info = new DataSetInformation();
		String name = "xtest UnitGroup - " + new Random().nextInt(1000);
		LangString.addLabel(info.getName(), name);
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