package org.openlca.ilcd.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.descriptors.UnitGroupDescriptor;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.io.XmlBinder;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.util.UnitGroupBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class DescriptorTest {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String baseUrl = "http://localhost:8080/soda4LCA/resource";
	private String unitUrl = baseUrl + "/unitgroups";
	Client client = Client.create();

	@Before
	public void setUp() throws Exception {
		NetworkClient client = new NetworkClient(baseUrl, "admin", "default");
		client.connect();
		XmlBinder binder = new XmlBinder();
		UnitGroup group = binder.fromStream(UnitGroup.class, getClass()
				.getResourceAsStream("unit.xml"));
		UnitGroupBag bag = new UnitGroupBag(group);
		if (client.contains(UnitGroup.class, bag.getId()))
			return;
		client.put(group, group.getUnitGroupInformation()
				.getDataSetInformation().getUUID());
	}

	@Test
	public void testGetDescriptors() {
		log.trace("Run testGetDescriptors");
		log.trace("Get unit groups: {}", unitUrl);
		DescriptorList result = client.resource(unitUrl).get(
				DescriptorList.class);
		assertTrue(result.getDescriptors().size() > 0);
		iterateAndCompareFirst(result);
	}

	private void iterateAndCompareFirst(DescriptorList result) {
		for (Object obj : result.getDescriptors()) {
			assertTrue(obj instanceof UnitGroupDescriptor);
			UnitGroupDescriptor descriptor = (UnitGroupDescriptor) obj;
			log.trace("Unit group '{}' found.", descriptor.getName().getValue());
		}
		UnitGroupDescriptor descriptorFromList = (UnitGroupDescriptor) result
				.getDescriptors().get(0);
		compareFirst(descriptorFromList);
		loadFull(descriptorFromList);
	}

	private void compareFirst(UnitGroupDescriptor descriptorFromList) {
		WebResource resource = client.resource(unitUrl)
				.path(descriptorFromList.getUuid())
				.queryParam("view", "overview");
		log.trace("Get unit group descriptor: {}", resource.getURI());
		UnitGroupDescriptor descriptor = resource
				.get(UnitGroupDescriptor.class);
		compareDescriptors(descriptorFromList, descriptor);
	}

	private void compareDescriptors(UnitGroupDescriptor expected,
			UnitGroupDescriptor actual) {
		assertEquals(expected.getName().getValue(), actual.getName().getValue());
		assertEquals(expected.getUuid(), actual.getUuid());
	}

	private void loadFull(UnitGroupDescriptor descriptor) {
		WebResource resource = client.resource(unitUrl)
				.path(descriptor.getUuid()).queryParam("format", "xml");
		log.trace("Get full unit group: {}", resource.getURI());
		UnitGroup unitGroup = resource.get(UnitGroup.class);
		assertEquals(descriptor.getName().getValue(), unitGroup
				.getUnitGroupInformation().getDataSetInformation().getName()
				.get(0).getValue());
		assertEquals(descriptor.getUuid(), unitGroup.getUnitGroupInformation()
				.getDataSetInformation().getUUID());
	}

}
