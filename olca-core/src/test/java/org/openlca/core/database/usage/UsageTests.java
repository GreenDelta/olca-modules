package org.openlca.core.database.usage;

import org.junit.Assert;
import org.openlca.core.Tests;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

import java.util.List;

class UsageTests {

	private UsageTests() {
	}

	static void expectEmpty(RootEntity entity) {
		var deps1 = UsageSearch.find(Tests.getDb(), entity);
		var deps2 = UsageSearch.find(Tests.getDb(), Descriptor.of(entity));
		Assert.assertTrue(deps1.isEmpty());
		Assert.assertTrue(deps2.isEmpty());
	}

	static void expectOne(RootEntity entity, RootEntity dependent) {
		var deps1 = UsageSearch.find(Tests.getDb(), entity);
		var deps2 = UsageSearch.find(Tests.getDb(), Descriptor.of(entity));
		for (var deps : List.of(deps1, deps2)) {
			Assert.assertEquals(1, deps.size());
			Assert.assertEquals(Descriptor.of(dependent), deps.iterator().next());
		}
	}

}
