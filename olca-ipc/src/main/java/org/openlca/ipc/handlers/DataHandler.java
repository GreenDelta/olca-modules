package org.openlca.ipc.handlers;

import java.util.function.BiFunction;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonRef;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonObject;

public class DataHandler {

	private final IDatabase db;
	private final JsonDataService service;

	public DataHandler(HandlerContext context) {
		this.db = context.db();
		this.service = new JsonDataService(db);
	}

	@Rpc("data/get/descriptors")
	public RpcResponse getDescriptors(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getDescriptors(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("data/get")
	public RpcResponse get(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var id = JsonRef.idOf(json);
			if (Strings.notEmpty(id))
				return Responses.of(service.get(type.getModelClass(), id), req);
			var name = Json.getString(json, "name");
			if (Strings.notEmpty(name))
				return Responses.of(service.getForName(type.getModelClass(), name), req);
			return Responses.badRequest("no id or name provided", req);
		});
	}

	@Rpc("data/get/descriptor")
	public RpcResponse getDescriptor(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var clazz = type.getModelClass();
			var id = JsonRef.idOf(json);
			if (Strings.notEmpty(id)) {
				var resp = service.getDescriptor(clazz, id);
				return Responses.of(resp, req);
			}
			var name = Json.getString(json, "name");
			if (Strings.notEmpty(name)) {
				var resp = service.getDescriptorForName(clazz, name);
				return Responses.of(resp, req);
			}
			return Responses.invalidParams("@id or name must be provided", req);
		});
	}

	@Rpc("data/get/all")
	public RpcResponse getAll(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getAll(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("data/put")
	public RpcResponse put(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.put(type.getModelClass(), json);
			return Responses.of(resp, req);
		});
	}

	@Rpc("data/delete")
	public RpcResponse delete(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.delete(type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("data/get/providers")
	public RpcResponse getProviders(RpcRequest req) {
		var flowId = req.params != null && req.params.isJsonObject()
				? JsonRef.idOf(req.params.getAsJsonObject())
				: null;
		if (Strings.nullOrEmpty(flowId)) {
			var resp = service.getProviders();
			return Responses.of(resp, req);
		} else {
			var resp = service.getProvidersOfFlow(flowId);
			return Responses.of(resp, req);
		}
	}

	@Rpc("data/get/parameters")
	public RpcResponse getParameters(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getParametersOf(
					type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	// TODO: unclear if we should keep this in the IPC protocol, as we can just
	// calculate processes now; the creation of product systems is not required
	// anymore; however, if specific linking options should be applied, then this
	// is required; but are there users for that?
	@Rpc("create/product_system")
	public RpcResponse createProductSystem(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with valid processId", req);
		var obj = req.params.getAsJsonObject();
		if (!obj.has("processId") || !obj.get("processId").isJsonPrimitive())
			return Responses.invalidParams("params must be an object with valid processId", req);
		var processId = obj.get("processId").getAsString();
		if (Strings.nullOrEmpty(processId))
			return Responses.invalidParams("params must be an object with valid processId", req);
		var refProcess = new ProcessDao(db).getForRefId(processId);
		if (refProcess == null)
			return Responses.invalidParams("No process found for ref id " + processId, req);
		var system = ProductSystem.of(refProcess);
		system = new ProductSystemDao(db).insert(system);
		var config = new LinkingConfig()
				.preferredType(ProcessType.UNIT_PROCESS);
		if (obj.has("preferredType") && obj.get("preferredType").getAsString().equalsIgnoreCase("lci_result")) {
			config.preferredType(ProcessType.LCI_RESULT);
		}
		config.providerLinking(ProviderLinking.PREFER_DEFAULTS);
		if (obj.has("providerLinking")) {
			if (obj.get("providerLinking").getAsString().equalsIgnoreCase("ignore")) {
				config.providerLinking(ProviderLinking.IGNORE_DEFAULTS);
			} else if (obj.get("providerLinking").getAsString().equalsIgnoreCase("only")) {
				config.providerLinking(ProviderLinking.ONLY_DEFAULTS);
			}
		}
		var builder = new ProductSystemBuilder(MatrixCache.createLazy(db), config);
		builder.autoComplete(system);
		system = ProductSystemBuilder.update(db, system);
		var res = new JsonObject();
		res.addProperty("@id", system.refId);
		return Responses.ok(res, req);
	}

	private RpcResponse withTypedParam(
			RpcRequest req, BiFunction<JsonObject, ModelType, RpcResponse> fn) {
		var r = req.requireJsonObject();
		if (r.isError())
			return Responses.badRequest(r.error(), req);
		var type = JsonRef.typeOf(r.value());
		if (type == null)
			return Responses.invalidParams(
					"no valid @type attribute present in request", req);
		return fn.apply(r.value(), type);
	}

}
