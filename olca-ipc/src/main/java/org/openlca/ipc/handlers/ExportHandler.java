package org.openlca.ipc.handlers;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.math.Simulator;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimulationResult;
import org.openlca.io.xls.results.SimulationResultExport;
import org.openlca.io.xls.results.system.ResultExport;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public class ExportHandler {

	private final HandlerContext context;

	public ExportHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("export/excel")
	public RpcResponse excel(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.badRequest("No @id given", req);
		JsonObject obj = req.params.getAsJsonObject();
		String id = Json.getString(obj, "@id");
		if (id == null)
			return Responses.badRequest("No `@id` given", req);
		String path = Json.getString(obj, "path");
		if (path == null)
			return Responses.badRequest("No `path` given", req);
		Object val = context.cache.get(id);
		if (!(val instanceof CachedResult))
			return Responses.notImplemented("The Excel export is currently"
					+ " only implemented for calculation results", req);
		CachedResult<?> r = (CachedResult<?>) val;
		if (r.result instanceof SimpleResult)
			return exportSimpleResult(req, path, r);
		if (r.result instanceof Simulator)
			return exportSimulationResult(req, path, r);
		return Responses.notImplemented("The Excel export is currently"
				+ " only implemented for calculation results", req);
	}

	private RpcResponse exportSimpleResult(RpcRequest req, String path,
			CachedResult<?> r) {
		ResultExport export = new ResultExport(r.setup,
				(SimpleResult) r.result,
				new File(path),
				EntityCache.create(context.db));
		export.run();
		if (export.doneWithSuccess())
			return Responses.ok("Exported to " + path, req);
		else
			return Responses.internalServerError("Export failed", req);
	}

	private RpcResponse exportSimulationResult(RpcRequest req, String path,
			CachedResult<?> r) {
		Simulator simulator = (Simulator) r.result;
		SimulationResult result = simulator.getResult();
		SimulationResultExport export = new SimulationResultExport(
				r.setup, result, context.db);
		try {
			export.run(new File(path));
			return Responses.ok("Exported to " + path, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

}
