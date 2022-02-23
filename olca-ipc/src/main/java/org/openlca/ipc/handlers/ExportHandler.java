package org.openlca.ipc.handlers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.Daos;
import org.openlca.core.database.EntityCache;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.ModelType;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimulationResult;
import org.openlca.io.xls.results.SimulationResultExport;
import org.openlca.io.xls.results.system.ResultExport;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
				r.setup, result, EntityCache.create(context.db));
		try {
			export.run(new File(path));
			return Responses.ok("Exported to " + path, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("export/json-ld")
	public RpcResponse jsonLd(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.badRequest("No @id given", req);
		JsonObject obj = req.params.getAsJsonObject();
		String path = Json.getString(obj, "path");
		if (path == null)
			return Responses.badRequest("No `path` given", req);
		Map<ModelType, Set<String>> toExport = getModels(obj);
		if (toExport == null)
			return Responses.badRequest("No `models` given", req);
		try {
			var store = ZipStore.open(new File(path));
			var export = new JsonExport(context.db, store);
			for (ModelType type : toExport.keySet()) {
				for (String refId : toExport.get(type)) {
					export.write(Daos.root(context.db, type).getForRefId(refId));
				}
			}
			store.close();
			return Responses.ok("Exported to " + path, req);
		} catch (IOException e) {
			return Responses.serverError(e, req);
		}
	}

	private Map<ModelType, Set<String>> getModels(JsonObject obj) {
		JsonArray models = Json.getArray(obj, "models");
		if (models == null)
			return null;
		Map<ModelType, Set<String>> map = new HashMap<>();
		for (JsonElement e : models) {
			if (!e.isJsonObject())
				continue;
			JsonObject model = e.getAsJsonObject();
			String id = Json.getString(model, "@id");
			String type = Json.getString(model, "@type");
			if (id == null || type == null)
				continue;
			for (ModelType t : ModelType.values()) {
				if (t.getModelClass() != null && t.getModelClass().getSimpleName().equals(type)) {
					Set<String> ids = map.get(t);
					if (ids == null) {
						ids = new HashSet<>();
						map.put(t, ids);
					}
					ids.add(id);
				}
			}
		}
		return map;
	}

}
