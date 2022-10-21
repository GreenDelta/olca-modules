package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.services.JsonCalculationSetup;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Pair;

import com.google.gson.JsonObject;

public class SimulationHandler {

	private final HandlerContext context;
	private final IDatabase db;

	public SimulationHandler(HandlerContext context) {
		this.context = context;
		this.db = context.db();
	}

	/**
	 * Creates a Monte-Carlo simulator for a calculation setup.
	 */
	@Rpc("simulator")
	public RpcResponse simulator(RpcRequest req) {
		var p = setupOf(req);
		if (p.second != null)
			return p.second;
		var setup = p.first;
		var simulator = Simulator.create(setup, db)
				.withLibraryDir(context.libDir());
		var id = context.cache(CachedResult.of(context, setup, simulator));
		var obj = new JsonObject();
		obj.addProperty("@id", id);
		obj.addProperty("@type", "Simulator");
		return Responses.ok(obj, req);
	}

	/**
	 * Runs a next Monte-Carlo-Simulation on a cached simulator. Returns the
	 * simulation result but does not cache it (as it is cached in the result of
	 * the simulator).
	 */
	@Rpc("next/simulation")
	public RpcResponse nextSimulation(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No simulator given", req);
		String id = Json.getString(req.params.getAsJsonObject(), "@id");
		if (id == null)
			return Responses.invalidParams("No simulator with '@id' given", req);
		var cached = context.getCached(CachedResult.class, id);
		if (cached == null)
			return Responses.invalidParams("No cached simulator with @id=" + id, req);
		if (!(cached.result() instanceof Simulator simulator))
			return Responses.invalidParams("No cached simulator with @id=" + id, req);
		var next = simulator.nextRun();
		if (next == null)
			return Responses.internalServerError("Simulation failed", req);
		return Responses.ok(JsonRpc.encodeResult(next, id, cached.refs()), req);
	}

	private Pair<CalculationSetup, RpcResponse> setupOf(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject()) {
			var err = Responses.invalidParams("No calculation setup given", req);
			return Pair.of(null, err);
		}
		var json = req.params.getAsJsonObject();
		var setup = JsonCalculationSetup.readFrom(json, DbEntityResolver.of(db));
		if (setup.hasError()) {
			var err = Responses.invalidParams(setup.error(), req);
			return Pair.of(null, err);
		}
		return Pair.of(setup.setup(), null);
	}
}
