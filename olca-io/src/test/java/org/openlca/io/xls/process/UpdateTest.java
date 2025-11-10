package org.openlca.io.xls.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.core.model.Version;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.io.Tests;
import org.openlca.jsonld.input.UpdateMode;

public class UpdateTest {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() {
		var mass = ProcTests.createMass(db);
		var p = Flow.product("p", mass);
		process = Process.of("P", p);
		process.documentation = new ProcessDoc();
		var root = Category.of("some", ModelType.PROCESS);
		process.category = Category.childOf(root, "category");
		db.insert(p, root, process);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testUpdateDqSchema() {

		var schema = db.insert(DQSystem.of("dq-schema"));
		process.dqSystem = schema;
		process.exchangeDqSystem = schema;
		process.socialDqSystem = schema;

		var v = new Version(process.version);
		v.incMinor();
		process.version = v.getValue();

		var synced = sync(UpdateMode.IF_NEWER);
		assertNotSame(process, synced);
		assertEquals(synced.dqSystem, schema);
		assertEquals(synced.exchangeDqSystem, schema);
		assertEquals(synced.socialDqSystem, schema);
	}

	@Test
	public void testUpdateCosts() {

		var currency = Currency.of("EUR");
		currency.referenceCurrency = currency;
		// db.insert(currency);

		var qref = process.quantitativeReference;
		qref.currency = currency;
		qref.costs = 42.0;

		var synced = sync(UpdateMode.ALWAYS);
		qref = synced.quantitativeReference;
		assertEquals("EUR", qref.currency.name);
		assertEquals(qref.costs, 42.0, 1e-17);
	}

	@Test
	public void testUncertainty() {
		var mass = db.getForName(FlowProperty.class, "Mass");
		var flow = Flow.elementary("e", mass);

		for (var type : UncertaintyType.values()) {
			if (type == UncertaintyType.NONE)
				continue;
			var e = process.output(flow, 0.42);
			e.uncertainty = switch (type) {
				case NORMAL -> Uncertainty.normal(0.42, 0.2);
				case LOG_NORMAL -> Uncertainty.logNormal(0.42, 1.2);
				case UNIFORM -> Uncertainty.uniform(0.2, 0.8);
				case TRIANGLE -> Uncertainty.triangle(0.2, 0.7, 0.8);
				default -> null;
			};
		}

		var synced = sync(UpdateMode.ALWAYS);
		for (var type : UncertaintyType.values()) {
			if (type == UncertaintyType.NONE)
				continue;
			var u = synced.exchanges.stream()
				.filter(e -> e.uncertainty.distributionType == type)
				.map(e -> e.uncertainty)
				.findAny()
				.orElseThrow();
			switch (type) {
				case NORMAL -> {
					assertEquals(0.42, u.parameter1, 1e-17);
					assertEquals(0.2, u.parameter2, 1e-17);
				}
				case LOG_NORMAL -> {
					assertEquals(0.42, u.parameter1, 1e-17);
					assertEquals(1.2, u.parameter2, 1e-17);
				}
				case UNIFORM -> {
					assertEquals(0.2, u.parameter1, 1e-17);
					assertEquals(0.8, u.parameter2, 1e-17);
				}
				case TRIANGLE -> {
					assertEquals(0.2, u.parameter1, 1e-17);
					assertEquals(0.7, u.parameter2, 1e-17);
					assertEquals(0.8, u.parameter3, 1e-17);
				}
				case NONE -> {
				}
			}
		}
	}

	private Process sync(UpdateMode mode) {
		try {
			var file = Files.createTempFile("_olca_", ".xlsx").toFile();
			XlsProcessWriter.of(db).write(process, file);
			var synced = XlsProcessReader.of(db)
				.withUpdates(mode)
				.sync(file)
				.orElseThrow();
			// System.out.println(file.getAbsolutePath());
			Files.delete(file.toPath());
			return synced;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
