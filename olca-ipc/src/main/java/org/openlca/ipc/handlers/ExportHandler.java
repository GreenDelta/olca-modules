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
import org.openlca.io.xls.results.SimulationResultExport;
import org.openlca.io.xls.results.system.ResultExport;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;

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
		var r = context.getCached(CachedResult.class, id);
		if (r == null)
			return Responses.notImplemented("The Excel export is currently"
				+ " only implemented for calculation results", req);
		if (r.result() instanceof SimpleResult)
			return exportSimpleResult(req, path, r);
		if (r.result() instanceof Simulator)
			return exportSimulationResult(req, path, r);
		return Responses.notImplemented("The Excel export is currently"
			+ " only implemented for calculation results", req);
	}

	private RpcResponse exportSimpleResult(RpcRequest req, String path,
	                                       CachedResult<?> r) {
		var export = new ResultExport(
			r.setup(),
			(SimpleResult) r.result(),
			new File(path),
			EntityCache.create(context.db()));
		export.run();
		if (export.doneWithSuccess())
			return Responses.ok("Exported to " + path, req);
		else
			return Responses.internalServerError("Export failed", req);
	}

	private RpcResponse exportSimulationResult(RpcRequest req, String path,
	                                           CachedResult<?> r) {
		var simulator = (Simulator) r.result();
		var result = simulator.getResult();
		var export = new SimulationResultExport(
			r.setup(), result, EntityCache.create(context.db()));
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
		var obj = req.params.getAsJsonObject();
		var path = Json.getString(obj, "path");
		if (path == null)
			return Responses.badRequest("No `path` given", req);
		var models = getModels(obj);
		if (models.isEmpty())
			return Responses.badRequest("No `models` given", req);
		try {
			var store = ZipStore.open(new File(path));
			var export = new JsonExport(context.db(), store);
			for (var type : models.keySet()) {
				for (var refId : models.get(type)) {
					export.write(Daos.root(context.db(), type).getForRefId(refId));
				}
			}
			store.close();
			return Responses.ok("Exported to " + path, req);
		} catch (IOException e) {
			return Responses.serverError(e, req);
		}
	}

	private Map<ModelType, Set<String>> getModels(JsonObject obj) {
		var map = new HashMap<ModelType, Set<String>>();
		Json.forEachObject(obj, "models", model -> {
			String id = Json.getString(model, "@id");
			String type = Json.getString(model, "@type");
			if (id == null || type == null)
				return;
			for (ModelType t : ModelType.values()) {
				var clazz = t.getModelClass();
				if (clazz != null && clazz.getSimpleName().equals(type)) {
					var ids = map.computeIfAbsent(t, k -> new HashSet<>());
					ids.add(id);
				}
			}
		});
		return map;
	}

}
