package org.openlca.ipc.handlers;

import java.util.function.BiFunction;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonRef;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.DbRefs;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ModelHandler {

	private final IDatabase db;
	private final JsonDataService jsonData;

	public ModelHandler(HandlerContext context) {
		this.db = context.db();
		this.jsonData = new JsonDataService(db);
	}

	@Rpc("get/model")
	public RpcResponse get(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.get(type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/models")
	public RpcResponse getAll(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.getAll(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/descriptors")
	public RpcResponse getDescriptors(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.getDescriptors(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/descriptor")
	public RpcResponse getDescriptor(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.getDescriptor(
					type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("insert/model")
	public RpcResponse insert(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.put(type.getModelClass(), json);
			return Responses.of(resp, req);
		});
	}

	@Rpc("update/model")
	public RpcResponse update(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.put(type.getModelClass(), json);
			return Responses.of(resp, req);
		});
	}

	@Rpc("delete/model")
	public RpcResponse delete(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var resp = jsonData.delete(type.getModelClass(), JsonRef.idOf(json));
			return Responses.of(resp, req);
		});
	}

	@Rpc("get/providers")
	public RpcResponse getProviders(RpcRequest req) {
		var d = descriptorOf(req);
		if (d == null || d.refId == null)
			return Responses.invalidParams(
					"A valid flow reference with a valid @id is required", req);

		var flow = db.get(Flow.class, d.refId);
		if (flow == null)
			return Responses.notFound(
					"No flow with @id='" + d.refId + "' exists", req);

		var providers = ProcessTable.create(db).getProviders(flow.id);
		var refs = DbRefs.of(db);
		var array = providers.stream()
				.map(TechFlow::provider)
				.map(refs::asRef)
				.collect(JsonRpc.toArray());
		return Responses.ok(array, req);
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




	private Descriptor descriptorOf(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return null;
		var obj = req.params.getAsJsonObject();
		var type = getType(obj);
		if (type == null)
			return null;
		var d = new Descriptor();
		d.type = type;
		d.refId = Json.getString(obj, "@id");
		d.name = Json.getString(obj, "name");
		return d;
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
