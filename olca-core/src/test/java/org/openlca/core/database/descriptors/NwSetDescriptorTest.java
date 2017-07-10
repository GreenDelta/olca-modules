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
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.NwSetDescriptor;

public class NwSetDescriptorTest {

	private NwSetDao dao = new NwSetDao(Tests.getDb());
	private NwSet nwSet;

	@Before
	public void setUp() {
		nwSet = new NwSet();
		nwSet.setName("#test#-name");
		nwSet.setDescription("#test#-descr");
		nwSet.setRefId(UUID.randomUUID().toString());
		nwSet.weightedScoreUnit = "#test#-unit";
		nwSet = dao.insert(nwSet);
	}

	@After
	public void tearDown() {
		dao.delete(nwSet);
	}

	@Test
	public void testToDescriptor() {
		NwSetDescriptor descriptor = (NwSetDescriptor) Descriptors
				.toDescriptor(nwSet);
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetForId() {
		NwSetDescriptor descriptor = dao.getDescriptor(nwSet.getId());
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetFromAll() {
		NwSetDescriptor descriptor = null;
		for (NwSetDescriptor candidate : dao.getDescriptors()) {
			if (candidate.getId() == nwSet.getId())
				descriptor = candidate;
		}
		checkDescriptor(descriptor);
	}

	@Test
	public void testGetForIdSet() {
		NwSetDescriptor descriptor = null;
		for (NwSetDescriptor candidate : dao.getDescriptors(Collections
				.singleton(nwSet.getId()))) {
			if (candidate.getId() == nwSet.getId())
				descriptor = candidate;
		}
		checkDescriptor(descriptor);
	}

	private void checkDescriptor(NwSetDescriptor descriptor) {
		Assert.assertEquals(nwSet.getName(), descriptor.getName());
		Assert.assertEquals(nwSet.getDescription(), descriptor.getDescription());
		Assert.assertEquals(nwSet.getRefId(), descriptor.getRefId());
		Assert.assertEquals(nwSet.weightedScoreUnit,
				descriptor.getWeightedScoreUnit());
	}

}
