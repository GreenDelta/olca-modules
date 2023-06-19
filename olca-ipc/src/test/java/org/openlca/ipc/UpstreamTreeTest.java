package org.openlca.ipc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@Ignore
public class UpstreamTreeTest {

	private final IDatabase db = Tests.getDb();
	private String resultId;
	private final AtomicInteger _reqId = new AtomicInteger(42);
	private List<? extends RootEntity> entities;

	@Before
	public void setup() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var eur = Currency.of("EUR");
		var e = Flow.elementary("e", mass);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		db.insert(units, mass, e, p, q, eur);

		var pP = Process.of("P", p);
		var eP = pP.output(e, 0.5);
		eP.costs = 1.0;
		eP.currency = eur;

		var pQ = Process.of("Q", q);
		pQ.input(p, 2);
		var eQ = pQ.output(e, 2.0);
		eQ.costs = 4.0;
		eQ.currency = eur;
		db.insert(pP, pQ);

		var sys = ProductSystem.of(pQ);
		sys.link(pP, pQ);
		db.insert(sys);

		entities = List.of(sys, pQ, pP, q, p, e, eur, mass, units);
		// calculate and wait for result
		var state = nextRequest("result/calculate", obj -> {
			obj.add("target", Json.asRef(sys));
			obj.addProperty("withCosts", true);
		}).getAsJsonObject();
		resultId = Json.getString(state, "@id");
		int calls = 0;
		while (!Json.getBool(state, "isReady", false)) {
			calls++;
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			if (calls > 100) {
				fail("More that 100 calls and still no result");
			}
			state = nextRequest(
					"result/state", obj -> Json.put(obj, "@id", resultId))
					.getAsJsonObject();
		}
	}

	@After
	public void cleanup() {
		nextRequest("result/dispose", obj -> Json.put(obj, "@id", resultId));
		entities.forEach(db::delete);
	}

	@Test
	public void testFlowTree() {
		var flow = entities.stream()
				.filter(e -> e instanceof Flow && e.name.equals("e"))
				.findAny()
				.orElseThrow();
		var enviFlow = new Gson().toJsonTree(
				Map.of("flow",
						Map.of("@id", flow.refId)));

		// test the root node
		var roots = nextRequest("result/upstream-interventions-of", req -> {
			req.addProperty("@id", resultId);
			req.add("enviFlow", enviFlow);
		}).getAsJsonArray();
		var root = roots.get(0).getAsJsonObject();
		assertEquals(3.0, Json.getDouble(root, "result", 0), 1e-10);
		assertEquals(2.0, Json.getDouble(root, "directContribution", 0), 1e-10);

		// test the child node
		var rootFlow = root.get("techFlow").getAsJsonObject();
		var path = Json.getRefId(rootFlow, "provider")
				+ "::" + Json.getRefId(rootFlow, "flow");
		var childs = nextRequest("result/upstream-interventions-of", req -> {
			req.addProperty("@id", resultId);
			req.addProperty("path", path);
			req.add("enviFlow", enviFlow);
		}).getAsJsonArray();
		var child = childs.get(0).getAsJsonObject();
		assertEquals(1.0, Json.getDouble(child, "result", 0), 1e-10);
		assertEquals(1.0, Json.getDouble(child, "directContribution", 0), 1e-10);
	}

	private JsonElement nextRequest(String method, Consumer<JsonObject> params) {
		var req = new RpcRequest();
		req.jsonrpc = "2.0";
		req.id = new JsonPrimitive(_reqId.incrementAndGet());
		req.method = method;
		var p = new JsonObject();
		params.accept(p);
		req.params = p;
		var result = Tests.post(req);
		if (result.error != null) {
			fail("calling method: '" + method
					+ "' produced error: " + result.error.message);
		}
		return result.result;
	}

}
