package org.openlca.core.database.usage;

import org.junit.Assert;
import org.openlca.core.Tests;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

class UsageTests {

	private UsageTests() {
	}

	static void expectEmpty(RootEntity entity) {
		var deps = IUseSearch.find(Tests.getDb(), entity);
		Assert.assertTrue(deps.isEmpty());
	}

	static void expectOne(RootEntity entity, RootEntity dependent) {
		var deps = IUseSearch.find(Tests.getDb(), entity);
		Assert.assertEquals(1, deps.size());
		Assert.assertEquals(Descriptor.of(dependent), deps.iterator().next());
	}

}
