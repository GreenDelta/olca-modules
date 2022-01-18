package org.openlca.core.database.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Source;

/**
 * Test the updates for the database schema v8.
 */
public class Update8Test {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testImpactMethods() {
		var source = Source.of("source");
		var method = new ImpactMethod();
		method.source = source;
		db.insert(source, method);
		var clone = db.get(ImpactMethod.class, method.id);
		assertEquals("source", clone.source.name);
		db.delete(method, source);
	}

	@Test
	public void testProjectVariants() {
		ProjectDao dao = new ProjectDao(db);
		Project project = new Project();
		ProjectVariant var = new ProjectVariant();
		var.isDisabled = true;
		project.variants.add(var);
		dao.insert(project);

		project = dao.getForId(project.id);
		assertTrue(project.variants.get(0).isDisabled);
		project.variants.get(0).isDisabled = false;
		dao.update(project);
		project = dao.getForId(project.id);
		assertFalse(project.variants.get(0).isDisabled);
		dao.delete(project);
	}

	@Test
	public void testProcessDoc() {
		Source source = new Source();
		source.name = "source";
		Daos.base(db, Source.class).insert(source);

		ProcessDao dao = new ProcessDao(db);
		Process proc = new Process();
		proc.documentation = new ProcessDocumentation();
		proc.documentation.sources.add(source);
		proc.documentation.precedingDataSet = "ABC123";
		dao.insert(proc);

		proc = dao.getForId(proc.id);
		assertEquals("source", proc.documentation.sources.get(0).name);
		assertEquals("ABC123", proc.documentation.precedingDataSet);
		dao.delete(proc);
		Daos.base(db, Source.class).delete(source);
	}

	@Test
	public void testSystemLink() {
		ProductSystemDao dao = new ProductSystemDao(db);
		ProductSystem system = new ProductSystem();
		ProcessLink link = new ProcessLink();
		link.providerType = ProcessLink.ProviderType.SUB_SYSTEM;
		system.processLinks.add(link);
		dao.insert(system);

		system = dao.getForId(system.id);
		assertTrue(system.processLinks.get(0).hasSubSystemProvider());
		dao.delete(system);
	}
}
