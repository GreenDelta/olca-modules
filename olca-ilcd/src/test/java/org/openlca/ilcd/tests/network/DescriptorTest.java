package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.descriptors.UnitGroupDescriptor;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.XmlBinder;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroupBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class DescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String unitUrl = Network.RESOURCE_URL + "/unitgroups";
	private Client client = Client.create();

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		SodaClient client = Network.createClient();
		XmlBinder binder = new XmlBinder();
		UnitGroup group = binder.fromStream(UnitGroup.class, getClass()
				.getResourceAsStream("unit.xml"));
		UnitGroupBag bag = new UnitGroupBag(group, "en");
		if (client.contains(UnitGroup.class, bag.getId()))
			return;
		client.put(group);
	}

	@Test
	public void testGetDescriptors() {
		Assume.assumeTrue(Network.isAppAlive());
		log.trace("Run testGetDescriptors");
		log.trace("Get unit groups: {}", unitUrl);
		DescriptorList result = client.resource(unitUrl).get(
				DescriptorList.class);
		assertTrue(result.descriptors.size() > 0);
		iterateAndCompareFirst(result);
	}

	private void iterateAndCompareFirst(DescriptorList result) {
		for (Object obj : result.descriptors) {
			assertTrue(obj instanceof UnitGroupDescriptor);
			UnitGroupDescriptor descriptor = (UnitGroupDescriptor) obj;
			log.trace("Unit group '{}' found.", descriptor.uuid);
		}
		UnitGroupDescriptor descriptorFromList = (UnitGroupDescriptor) result.descriptors.get(0);
		compareFirst(descriptorFromList);
		loadFull(descriptorFromList);
	}

	private void compareFirst(UnitGroupDescriptor descriptorFromList) {
		WebResource resource = client.resource(unitUrl)
				.path(descriptorFromList.uuid)
				.queryParam("view", "overview");
		log.trace("Get unit group descriptor: {}", resource.getURI());
		UnitGroupDescriptor descriptor = resource
				.get(UnitGroupDescriptor.class);
		compareDescriptors(descriptorFromList, descriptor);
	}

	private void compareDescriptors(UnitGroupDescriptor expected,
			UnitGroupDescriptor actual) {
		assertEquals(expected.name.get(0), actual.name.get(0));
		assertEquals(expected.uuid, actual.uuid);
	}

	private void loadFull(UnitGroupDescriptor descriptor) {
		WebResource resource = client.resource(unitUrl)
				.path(descriptor.uuid).queryParam("format", "xml");
		log.trace("Get full unit group: {}", resource.getURI());
		UnitGroup unitGroup = resource.get(UnitGroup.class);
		assertEquals(descriptor.name.get(0), unitGroup.unitGroupInfo.dataSetInfo.name
				.get(0).value);
		assertEquals(descriptor.uuid, unitGroup.unitGroupInfo.dataSetInfo.uuid);
	}

}
