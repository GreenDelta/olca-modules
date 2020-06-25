package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<SourceDescriptor> search;
	private Source source;
	private Process process;
	private ImpactMethod method;
	private ProcessDao processDao;
	private ImpactMethodDao methodDao;
	private DQSystem dqSystem;
	private DQSystemDao dqSystemDao;

	@Before
	public void setUp() {
		this.search = IUseSearch.FACTORY.createFor(ModelType.SOURCE, database);
		SourceDao sourceDao = new SourceDao(database);
		Source source = new Source();
		source.name = "test source";
		this.source = sourceDao.insert(source);
		processDao = new ProcessDao(database);
		Process process = new Process();
		process.name = "test process";
		ProcessDocumentation doc = new ProcessDocumentation();
		process.documentation = doc;
		this.process = processDao.insert(process);
		methodDao = new ImpactMethodDao(database);
		ImpactMethod method = new ImpactMethod();
		method.name = "test method";
		this.method = methodDao.insert(method);
		dqSystemDao = new DQSystemDao(database);
		DQSystem system = new DQSystem();
		this.dqSystem = dqSystemDao.insert(system);
	}

	@After
	public void tearDown() {
		processDao.delete(process);
		dqSystemDao.delete(dqSystem);
		SourceDao sourceDao = new SourceDao(database);
		sourceDao.delete(source);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProcessPublication() {
		process.documentation.publication = source;
		process = processDao.update(process);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(process), models.get(0));
	}

	@Test
	public void testFindInProcessSources() {
		process.documentation.sources.add(source);
		process = processDao.update(process);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(process), models.get(0));
	}

	@Test
	public void testFindInMethodSources() {
		method.sources.add(source);
		method = methodDao.update(method);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(method), models.get(0));
	}
	
	@Test
	public void testFindInDQSystem() {
		dqSystem.source = source;
		dqSystem = dqSystemDao.update(dqSystem);
		List<CategorizedDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(dqSystem), models.get(0));
	}

}
