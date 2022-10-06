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
import org.openlca.util.Strings;

import com.google.gson.JsonObject;

public class ModelHandler {

	private final IDatabase db;
	private final JsonDataService service;

	public ModelHandler(HandlerContext context) {
		this.db = context.db();
		this.service = new JsonDataService(db);
	}

	@Rpc("get/model")
	public RpcResponse get(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.get(type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/models")
	public RpcResponse getAll(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getAll(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/descriptors")
	public RpcResponse getDescriptors(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getDescriptors(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/descriptor")
	public RpcResponse getDescriptor(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.getDescriptor(
					type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("insert/model")
	public RpcResponse insert(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.put(type.getModelClass(), json);
			return Responses.of(resp, req);
		});
	}

	@Rpc("update/model")
	public RpcResponse update(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.put(type.getModelClass(), json);
			return Responses.of(resp, req);
		});
	}

	@Rpc("delete/model")
	public RpcResponse delete(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = service.delete(type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/providers")
	public RpcResponse getProviders(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			if (type != ModelType.FLOW)
				return Responses.badRequest("only valid for @type=Flow", req);
			var resp = service.getProvidersOfFlow(JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

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
