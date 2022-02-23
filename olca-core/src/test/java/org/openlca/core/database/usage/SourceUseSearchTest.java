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
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceUseSearchTest {

	private final IDatabase db = Tests.getDb();
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
		this.search = IUseSearch.FACTORY.createFor(ModelType.SOURCE, db);
		SourceDao sourceDao = new SourceDao(db);
		Source source = new Source();
		source.name = "test source";
		this.source = sourceDao.insert(source);
		processDao = new ProcessDao(db);
		Process process = new Process();
		process.name = "test process";
		process.documentation = new ProcessDocumentation();
		this.process = processDao.insert(process);
		methodDao = new ImpactMethodDao(db);
		ImpactMethod method = new ImpactMethod();
		method.name = "test method";
		this.method = methodDao.insert(method);
		dqSystemDao = new DQSystemDao(db);
		DQSystem system = new DQSystem();
		this.dqSystem = dqSystemDao.insert(system);
	}

	@After
	public void tearDown() {
		processDao.delete(process);
		dqSystemDao.delete(dqSystem);
		SourceDao sourceDao = new SourceDao(db);
		sourceDao.delete(source);
	}

	@Test
	public void testFindNoUsage() {
		var models = search.findUses(Descriptor.of(source));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInProcessPublication() {
		process.documentation.publication = source;
		process = processDao.update(process);
		var models = search.findUses(Descriptor.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(process), models.get(0));
	}

	@Test
	public void testFindInProcessSources() {
		process.documentation.sources.add(source);
		process = processDao.update(process);
		var models = search.findUses(Descriptor.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(process), models.get(0));
	}

	@Test
	public void testFindInImpactMethod() {
		method.source = source;
		method = methodDao.update(method);
		var models = search.findUses(Descriptor.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(method), models.get(0));
	}

	@Test
	public void testFindInImpactCategory() {
		var impact = ImpactCategory.of("impact");
		impact.source = source;
		db.insert(impact);
		var models = search.findUses(Descriptor.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(impact), models.get(0));
		db.delete(impact);
	}

	@Test
	public void testFindInDQSystem() {
		dqSystem.source = source;
		dqSystem = dqSystemDao.update(dqSystem);
		List<RootDescriptor> models = search.findUses(Descriptor
				.of(source));
		Assert.assertEquals(1, models.size());
		Assert.assertEquals(Descriptor.of(dqSystem), models.get(0));
	}

}
