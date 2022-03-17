package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;

public class SourceUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private Source source;
	private Process process;
	private ImpactMethod method;
	private DQSystem dqSystem;

	@Before
	public void setUp() {
		source = db.insert(Source.of("test source"));
		process = new Process();
		process.name = "test process";
		process.documentation = new ProcessDocumentation();
		process = db.insert(process);
		method = db.insert(ImpactMethod.of("test method"));
		dqSystem = db.insert( new DQSystem());
	}

	@After
	public void tearDown() {
		db.delete(process, dqSystem, method, source);
	}

	@Test
	public void testFindNoUsage() {
		UsageTests.expectEmpty(source);
	}

	@Test
	public void testFindInProcessPublication() {
		process.documentation.publication = source;
		process = db.update(process);
		UsageTests.expectOne(source, process);
	}

	@Test
	public void testFindInProcessSources() {
		process.documentation.sources.add(source);
		process = db.update(process);
		UsageTests.expectOne(source, process);
	}

	@Test
	public void testFindInImpactMethod() {
		method.source = source;
		method = db.update(method);
		UsageTests.expectOne(source, method);
	}

	@Test
	public void testFindInImpactCategory() {
		var impact = ImpactCategory.of("impact");
		impact.source = source;
		db.insert(impact);
		UsageTests.expectOne(source, impact);
		db.delete(impact);
	}

	@Test
	public void testFindInDQSystem() {
		dqSystem.source = source;
		dqSystem = db.update(dqSystem);
		UsageTests.expectOne(source, dqSystem);
	}

}
