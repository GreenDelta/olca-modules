package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;

public class SocialIndicatorUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<SocialIndicatorDescriptor> search;
	private SocialIndicator indicator;

	@Before
	public void setup() {
		indicator = new SocialIndicator();
		indicator.setName("indicator");
		indicator = database.createDao(SocialIndicator.class).insert(indicator);
		search = IUseSearch.FACTORY.createFor(ModelType.SOCIAL_INDICATOR,
				database);
	}

	@After
	public void tearDown() {
		database.createDao(SocialIndicator.class).delete(indicator);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(indicator));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(indicator));
		database.createDao(Process.class).delete(process);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.setName("process");
		SocialAspect aspect = new SocialAspect();
		aspect.indicator = indicator;
		process.socialAspects.add(aspect);
		return database.createDao(Process.class).insert(process);
	}
}
