package org.openlca.ipc.handlers;

import org.openlca.core.database.Daos;
import org.openlca.core.database.EntityCache;
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
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Pair;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ModelHandler {

	private final IDatabase db;

	public ModelHandler(HandlerContext context) {
		this.db = context.db;
	}

	@Rpc("get/model")
	public RpcResponse get(RpcRequest req) {
		try {
			var p = getModelOrError(req);
			if (p.second != null)
				return p.second;
			var model = p.first;
			var store = new MemStore();
			new JsonExport(db, store)
					.withReferences(false)
					.write(model);
			var modelType = ModelType.forModelClass(model.getClass());
			var obj = store.get(modelType, model.refId);
			if (obj == null)
				return Responses.error(500, "Conversion to JSON failed", req);
			return Responses.ok(obj, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("get/models")
	public RpcResponse getAll(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		var type = getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		try {
			var store = new MemStore();
			var exp = new JsonExport(db, store).withReferences(false);
			Daos.root(db, type).getAll().forEach(exp::write);
			var array = new JsonArray();
			store.getAll(type).forEach(array::add);
			return Responses.ok(array, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("get/descriptors")
	public RpcResponse getDescriptors(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		var type = getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		try {
			var array = new JsonArray();
			var cache = EntityCache.create(db);
			Daos.root(db, type).getDescriptors().forEach(d -> {
				JsonObject obj = JsonRef.of(d, cache);
				array.add(obj);
			});
			return Responses.ok(array, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("get/descriptor")
	public RpcResponse getDescriptor(RpcRequest req) {
		var p = getModelOrError(req);
		if (p.second != null)
			return p.second;
		var d = Descriptor.of(p.first);
		var json = JsonRef.of(d, EntityCache.create(db));
		return Responses.ok(json, req);
	}

	@Rpc("insert/model")
	public RpcResponse insert(RpcRequest req) {
		return saveModel(req, UpdateMode.NEVER);
	}

	@Rpc("update/model")
	public RpcResponse update(RpcRequest req) {
		return saveModel(req, UpdateMode.ALWAYS);
	}

	@Rpc("delete/model")
	public RpcResponse delete(RpcRequest req) {
		try {
			var p = getModelOrError(req);
			if (p.second != null)
				return p.second;
			db.delete(p.first);
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
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

		var providers = ProcessTable.create(db)
				.getProviders(flow.id);
		var array = new JsonArray();
		var cache = EntityCache.create(db);
		providers.stream()
				.map(TechFlow::provider)
				.filter(p -> p instanceof ProcessDescriptor)
				.map(p -> {
					var ref = JsonRef.of(p, cache);
					ref.addProperty("description", p.description);
					return ref;
				})
				.forEach(array::add);
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

	private RpcResponse saveModel(RpcRequest req, UpdateMode mode) {
		Descriptor d = descriptorOf(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @id and @type", req);
		JsonObject obj = req.params.getAsJsonObject();
		try {
			MemStore store = new MemStore();
			store.put(d.type, obj);
			JsonImport imp = new JsonImport(store, db);
			imp.setUpdateMode(mode);
			imp.run(d.type, d.refId);
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private Pair<RefEntity, RpcResponse> getModelOrError(RpcRequest req) {
		var d = descriptorOf(req);
		if (d == null || d.type == null) {
			var err = Responses.invalidParams(
					"could not identify model from request parameters", req);
			return Pair.of(null, err);
		}
		try {
			var type = d.type.getModelClass();

			// get by ID
			if (Strings.notEmpty(d.refId)) {
				var e = db.get(type, d.refId);
				if (e == null) {
					var err = Responses.error(404, "No " + type
							+ " with id='" + d.refId + "' found", req);
					return Pair.of(null, err);
				}
				return Pair.of(e, null);
			}

			// get by name
			if (Strings.notEmpty(d.name)) {
				var e = db.forName(type, d.name);
				if (e == null) {
					var err = Responses.error(404, "No " + type
							+ " with name='" + d.name + "' found", req);
					return Pair.of(null, err);
				}
				return Pair.of(e, null);
			}

			var err = Responses.invalidParams(
					"No valid ID or name given", req);
			return Pair.of(null, err);
		} catch (Exception e) {
			var err = Responses.serverError(e, req);
			return Pair.of(null, err);
		}
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

	private ModelType getType(JsonObject obj) {
		if (obj == null)
			return null;
		var s = Json.getString(obj, "@type");
		if (s == null)
			return null;
		try {
			var clazz = Class.forName("org.openlca.core.model." + s);
			return ModelType.forModelClass(clazz);
		} catch (Exception e) {
			return null;
		}
	}
}
