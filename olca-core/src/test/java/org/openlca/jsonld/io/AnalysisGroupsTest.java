package org.openlca.jsonld.io;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AnalysisGroup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class AnalysisGroupsTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testAnalysisGroups() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var r = Flow.product("r", mass);
		var P = Process.of("P", p);
		var Q = Process.of("Q", q);
		var R = Process.of("R", r);
		R.input(p, 2);
		R.input(q, 2);
		db.insert(units, mass, p, q, r, P, Q, R);

		var sys = ProductSystem.of(R);
		sys.link(P, R);
		sys.link(Q, R);

		var gA = new AnalysisGroup();
		gA.name = "A";
		gA.processes.add(P.id);
		gA.processes.add(Q.id);
		sys.analysisGroups.add(gA);

		var gB = new AnalysisGroup();
		gB.name = "B";
		gB.processes.add(R.id);
		sys.analysisGroups.add(gB);
		db.insert(sys);

		var store = new MemStore();
		new JsonExport(db, store).write(sys);
		db.clear();
		new JsonImport(store, db).run();

		P = db.get(Process.class, P.refId);
		Q = db.get(Process.class, Q.refId);
		R = db.get(Process.class, R.refId);
		sys = db.get(ProductSystem.class, sys.refId);
		for (var m : List.of(P, Q, R, sys)) {
			assertNotNull(m);
		}

		gA = sys.analysisGroups.stream()
				.filter(g -> g.name.equals("A"))
				.findAny()
				.orElse(null);
		assertNotNull(gA);
		gB = sys.analysisGroups.stream()
				.filter(g -> g.name.equals("B"))
				.findAny()
				.orElse(null);
		assertNotNull(gB);

		assertTrue(gA.processes.contains(P.id)
				&& gA.processes.contains(Q.id)
				&& gB.processes.contains(R.id));
		db.clear();
	}
}
