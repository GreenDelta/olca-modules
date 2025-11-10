package org.openlca.core.database.usage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openlca.core.Tests;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

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

	static void expectEach(RootEntity entity, RootEntity... dependents) {
		var deps1 = UsageSearch.find(Tests.getDb(), entity);
		var deps2 = UsageSearch.find(Tests.getDb(), Descriptor.of(entity));

		var expected = Arrays.stream(dependents)
				.map(Descriptor::of)
				.collect(Collectors.toSet());
		for (var deps : List.of(deps1, deps2)) {
			Assert.assertEquals(expected.size(), deps.size());
			for (var dep : deps) {
				Assert.assertTrue(expected.contains(dep));
			}
		}
	}

}
