package org.openlca.ilcd.tests.network;

import java.util.Random;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.Publication;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.util.IlcdConfig;
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
		UnitGroupInfo info = new UnitGroupInfo();
		unitgroup.unitGroupInformation = info;
		info.dataSetInformation = makeDataInfo(id);
		unitgroup.administrativeInformation = makeAdminInfo();
		client.put(unitgroup, id);
	}

	private DataSetInfo makeDataInfo(String id) {
		DataSetInfo info = new DataSetInfo();
		String name = "xtest UnitGroup - " + new Random().nextInt(1000);
		LangString.addLabel(info.name, name, IlcdConfig.getDefault());
		info.uuid = id;
		return info;
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		Publication pub = new Publication();
		info.publicationAndOwnership = pub;
		pub.dataSetVersion = "01.00.000";
		return info;
	}
}