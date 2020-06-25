package org.openlca.core.database.descriptors;

import java.util.Collections;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;

public class NwSetDescriptorTest {

	private NwSetDao dao = new NwSetDao(Tests.getDb());
	private NwSet nwSet;

	@Before
	public void setUp() {
		nwSet = new NwSet();
		nwSet.name = "#test#-name";
		nwSet.description = "#test#-descr";
		nwSet.refId = UUID.randomUUID().toString();
		nwSet.weightedScoreUnit = "#test#-unit";
		nwSet = dao.insert(nwSet);
	}

	@After
	public void tearDown() {
		dao.delete(nwSet);
	}

	@Test
	public void testToDescriptor() {
		NwSetDescriptor descriptor = (NwSetDescriptor) Descriptor
				.of(nwSet);
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetForId() {
		NwSetDescriptor descriptor = dao.getDescriptor(nwSet.id);
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetFromAll() {
		NwSetDescriptor descriptor = null;
		for (NwSetDescriptor candidate : dao.getDescriptors()) {
			if (candidate.id == nwSet.id)
				descriptor = candidate;
		}
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetForIdSet() {
		NwSetDescriptor descriptor = null;
		for (NwSetDescriptor candidate : dao.getDescriptors(Collections
				.singleton(nwSet.id))) {
			if (candidate.id == nwSet.id)
				descriptor = candidate;
		}
		checkDescriptor(descriptor);
	}

	private void checkDescriptor(NwSetDescriptor descriptor) {
		Assert.assertEquals(nwSet.name, descriptor.name);
		Assert.assertEquals(nwSet.description, descriptor.description);
		Assert.assertEquals(nwSet.refId, descriptor.refId);
		Assert.assertEquals(nwSet.weightedScoreUnit,
				descriptor.weightedScoreUnit);
	}

}
