package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.usage.IUseSearch;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<SourceDescriptor> search;
	private Source source;
	private Process process;
	private ProcessDao processDao;

	@Before
	public void setUp() {
		this.search = IUseSearch.FACTORY.createFor(ModelType.SOURCE, database);
		SourceDao sourceDao = new SourceDao(database);
		Source source = new Source();
		source.setName("test source");
		this.source = sourceDao.insert(source);
		processDao = new ProcessDao(database);
		Process process = new Process();
		process.setName("test process");
		ProcessDocumentation doc = new ProcessDocumentation();
		process.setDocumentation(doc);
		this.process = processDao.insert(process);
	}

	@After
	public void tearDown() {
		processDao.delete(process);
		SourceDao sourceDao = new SourceDao(database);
		sourceDao.delete(source);
	}

	@Test
	public void testFindNoUsage() {
		List<BaseDescriptor> models = search.findUses(Descriptors
				.toDescriptor(source));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProcessPublication() {
		process.getDocumentation().setPublication(source);
		process = processDao.update(process);
		List<BaseDescriptor> models = search.findUses(Descriptors
				.toDescriptor(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptors.toDescriptor(process), models.get(0));
	}

	@Test
	public void testFindInProcessSources() {
		process.getDocumentation().getSources().add(source);
		process = processDao.update(process);
		List<BaseDescriptor> models = search.findUses(Descriptors
				.toDescriptor(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptors.toDescriptor(process), models.get(0));
	}
}
