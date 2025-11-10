package org.openlca.core.matrix.cache;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

public class ProviderMapTest {

	private final IDatabase db = Tests.getDb();
	private List<RootEntity> entities;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var e = Flow.elementary("e", mass);

		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var r = Flow.product("r", mass);
		var w = Flow.waste("w", mass);

		var P = Process.of("P", p);
		P.output(e, 1);
		var Q = Process.of("Q", q);
		Q.output(e, 2);
		Q.output(r, 1);
		var R = Process.of("R", r);
		R.output(e, 3);
		var W = Process.of("W", w);
		W.output(e, 4);

		entities = List.of(units, mass, e, p, q, r, w, P, Q, R, W);
		for (var entity : entities) {
			db.insert(entity);
		}
	}

	@After
	public void cleanup() {
		for (var entity : entities.reversed()) {
			db.delete(entity);
		}
	}

	@Test
	public void testNoProvidersForElementaryFlow() {
		var map = ProviderMap.create(db);
		var providers = map.getProvidersOf(idOf("e"));
		assertTrue(providers.isEmpty());
		assertNull(map.getTechFlow(idOf("P"), idOf("e")));
	}

	private long idOf(String name) {
		for (var entity : entities) {
			if (name.equals(entity.name))
				return entity.id;
		}
		return 0;
	}
}
