package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.ilcd.descriptors.Descriptor;
import org.openlca.ilcd.descriptors.FlowDescriptor;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;

public class DescriptorLoadTest {

	@Test
	@Ignore
	public void testGetDescriptors() {
		SodaConnection con = new SodaConnection();
		con.url = "http://eplca.jrc.ec.europa.eu/ELCD3/resource";
		SodaClient client = new SodaClient(con);
		List<Descriptor> descriptors = client.getDescriptors(Flow.class);
		assertTrue(descriptors.size() > 40_000);

		// check all UUIDs are unique
		Set<String> ids = new HashSet<>();
		for (Descriptor d : descriptors) {
			assertTrue(d instanceof FlowDescriptor);
			if (ids.contains(d.uuid)) {
				fail("Duplicate UUID in descriptor list: " + d.uuid);
			}
			ids.add(d.uuid);
		}

		client.close();
	}

}
