package org.openlca.ipc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class UsageTest {

	private final IDatabase db = Tests.getDb();
	private final AtomicInteger nextId = new AtomicInteger(0);
	private List<? extends RootEntity> entities;

	private Flow p;
	private Flow unused;
	private Process pP;
	private ProductSystem sys;

	@Before
	public void setup() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		p = Flow.product("usage test p", mass);
		var q = Flow.product("usage test q", mass);
		unused = Flow.product("usage test unused", mass);
		db.insert(units, mass, p, q, unused);

		// P produces p; Q uses p as an input
		pP = Process.of("usage test P", p);
		var pQ = Process.of("usage test Q", q);
		pQ.input(p, 2);
		db.insert(pP, pQ);

		// a product system that contains P
		sys = ProductSystem.of(pQ);
		sys.link(pP, pQ);
		db.insert(sys);

		entities = List.of(sys, pQ, pP, unused, q, p, mass, units);
	}

	@After
	public void cleanup() {
		entities.forEach(db::delete);
	}

	@Test
	public void testFlowUsedByProcesses() {
		var names = usageOf("Flow", p.refId);
		assertTrue(names.contains("usage test P"));
		assertTrue(names.contains("usage test Q"));
	}

	@Test
	public void testUnusedFlow() {
		var names = usageOf("Flow", unused.refId);
		assertTrue(names.isEmpty());
	}

	@Test
	public void testProcessInProductSystem() {
		var names = usageOf("Process", pP.refId);
		assertTrue(names.contains(sys.name));
	}

	@Test
	public void testUnknownId() {
		var resp = Tests.post(request("Flow", "does-not-exist"));
		assertNull(resp.result);
		assertNotNull(resp.error);
		assertEquals(404, resp.error.code);
	}

	private Set<String> usageOf(String type, String id) {
		var resp = Tests.post(request(type, id));
		assertNull(resp.error);
		var names = new HashSet<String>();
		for (var ref : resp.result.getAsJsonArray()) {
			names.add(Json.getString(ref.getAsJsonObject(), "name"));
		}
		return names;
	}

	private RpcRequest request(String type, String id) {
		var req = new RpcRequest();
		req.jsonrpc = "2.0";
		req.id = new JsonPrimitive(nextId.incrementAndGet());
		req.method = "data/get/usage";
		var params = new JsonObject();
		params.addProperty("@type", type);
		params.addProperty("@id", id);
		req.params = params;
		return req;
	}
}
