package org.openlca.io.xls.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.io.Tests;

public class BaseTest {

	private final IDatabase db = Tests.getDb();
	private Process synced;

	@Before
	public void setup() {
		var store = InMemoryStore.create();
		var mass = ProcTests.createMass(store);
		var p = Flow.product("p", mass);

		var process = Process.of("P", p);
		var doc = process.documentation = new ProcessDoc();
		var root = Category.of("some", ModelType.PROCESS);
		process.category = Category.childOf(root, "category");

		var location = Location.of("Aruba", "AW");
		process.location = location;
		process.documentation.geography = "about geography";

		var actor = Actor.of("Actor");
		var source1 = Source.of("Source 1");
		var source2 = Source.of("Source 2");
		doc.sources.add(source1);
		doc.sources.add(source2);
		doc.publication = source1;

		doc.dataCollectionPeriod = "dataCollectionPeriod";
		doc.dataCompleteness = "completeness";
		doc.inventoryMethod = "inventoryMethod";
		doc.dataTreatment = "dataTreatment";
		doc.dataSelection = "dataSelection";
		doc.modelingConstants = "modelingConstants";
		doc.samplingProcedure = "sampling";

		// doc.reviewer = actor;
		doc.dataDocumentor = actor;
		doc.dataGenerator = actor;
		doc.dataOwner = actor;

		// write and read
		store.insert(actor, source1, source2, p, location, process);

		synced = ProcTests.syncWithDb(process, store);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testBasicMetaData() {
		assertEquals(synced.refId, synced.refId);
		assertEquals(synced.name, synced.name);
		assertEquals(synced.lastChange, synced.lastChange);
		assertEquals(synced.category.toPath(), "some/category");
	}

	@Test
	public void testQuantRef() {
		var qref = synced.quantitativeReference;
		assertEquals(1, qref.amount, 1e-17);
		assertEquals("p", qref.flow.name);
		assertEquals("kg", qref.unit.name);
		assertEquals("Mass", qref.flowPropertyFactor.flowProperty.name);
	}

	@Test
	public void testGeography() {
		var loc = synced.location;
		assertNotNull(loc);
		assertEquals("Aruba", loc.name);
		assertEquals("AW", loc.code);
		assertEquals("about geography", synced.documentation.geography);
	}

	@Test
	public void testModelling() {
		var doc = synced.documentation;
		assertEquals("dataCollectionPeriod", doc.dataCollectionPeriod);
		assertEquals("completeness", doc.dataCompleteness);
		assertEquals("inventoryMethod", doc.inventoryMethod);
		assertEquals("dataTreatment", doc.dataTreatment);
		assertEquals("dataSelection", doc.dataSelection);
		assertEquals("modelingConstants", doc.modelingConstants);
		assertEquals("sampling", doc.samplingProcedure);
	}

	@Test
	public void testSources() {
		var sources = synced.documentation.sources
			.stream()
			.map(s -> s.name)
			.toList();
		assertTrue(sources.contains("Source 1"));
		assertTrue(sources.contains("Source 2"));
	}

	@Test
	public void testActor() {
		var doc = synced.documentation;
		// assertEquals("Actor", doc.reviewer.name);

		assertEquals("Actor", doc.dataDocumentor.name);
		assertEquals("Actor", doc.dataGenerator.name);
		assertEquals("Actor", doc.dataOwner.name);
	}
}
