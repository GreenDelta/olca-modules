package org.openlca.ipc.handlers;

import org.openlca.core.database.Daos;
import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.LinkingConfig;
import org.openlca.core.matrix.LinkingConfig.DefaultProviders;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ModelHandler {

	private final IDatabase db;

	public ModelHandler(HandlerContext context) {
		this.db = context.db;
	}

	@Rpc("get/model")
	public RpcResponse get(RpcRequest req) {
		Descriptor d = readDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @id and @type", req);
		try {
			RootEntity e = Daos.root(db, d.type)
					.getForRefId(d.refId);
			if (e == null)
				return Responses.error(404, "Not found", req);
			MemStore store = new MemStore();
			JsonExport exp = new JsonExport(db, store);
			exp.setExportReferences(false);
			exp.write(e);
			JsonObject obj = store.get(d.type, d.refId);
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
		ModelType type = Models.getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		try {
			MemStore store = new MemStore();
			JsonExport exp = new JsonExport(db, store);
			exp.setExportReferences(false);
			Daos.root(db, type).getAll().forEach(exp::write);
			JsonArray array = new JsonArray();
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
		ModelType type = Models.getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @type attribute", req);
		try {
			JsonArray array = new JsonArray();
			EntityCache cache = EntityCache.create(db);
			Daos.root(db, type).getDescriptors().forEach(d -> {
				JsonObject obj = Json.asRef(d, cache);
				array.add(obj);
			});
			return Responses.ok(array, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
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
	@SuppressWarnings("unchecked")
	public <T extends RootEntity> RpcResponse delete(RpcRequest req) {
		Descriptor d = readDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with"
					+ " valid @id and @type", req);
		try {
			RootEntityDao<T, ?> dao = (RootEntityDao<T, ?>) Daos.root(
					db, d.type);
			T e = dao.getForRefId(d.refId);
			if (e == null)
				return Responses.error(404, "Not found", req);
			dao.delete(e);
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	@Rpc("create/product_system")
	public RpcResponse createProductSystem(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with valid processId", req);
		JsonObject obj = req.params.getAsJsonObject();
		if (!obj.has("processId") || !obj.get("processId").isJsonPrimitive())
			return Responses.invalidParams("params must be an object with valid processId", req);
		String processId = obj.get("processId").getAsString();
		if (Strings.isNullOrEmpty(processId))
			return Responses.invalidParams("params must be an object with valid processId", req);
		Process refProcess = new ProcessDao(db).getForRefId(processId);
		if (refProcess == null)
			return Responses.invalidParams("No process found for ref id " + processId, req);
		ProductSystem system = ProductSystem.of(refProcess);
		system = new ProductSystemDao(db).insert(system);
		LinkingConfig config = new LinkingConfig();
		config.preferredType = ProcessType.UNIT_PROCESS;
		if (obj.has("preferredType") && obj.get("preferredType").getAsString().toLowerCase().equals("lci_result")) {
			config.preferredType = ProcessType.LCI_RESULT;
		}
		config.providerLinking = DefaultProviders.PREFER;
		if (obj.has("providerLinking")) {
			if (obj.get("providerLinking").getAsString().toLowerCase().equals("ignore")) {
				config.providerLinking = DefaultProviders.IGNORE;				
			} else if (obj.get("providerLinking").getAsString().toLowerCase().equals("only")) {
				config.providerLinking = DefaultProviders.ONLY;								
			}
		}
		ProductSystemBuilder builder = new ProductSystemBuilder(MatrixCache.createLazy(db), config);
		builder.autoComplete(system);
		system = builder.saveUpdates(system);
		JsonObject res = new JsonObject();
		res.addProperty("@id", system.refId);
		return Responses.ok(res, req);
	}

	private RpcResponse saveModel(RpcRequest req, UpdateMode mode) {
		Descriptor d = readDescriptor(req);
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

	private Descriptor readDescriptor(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return null;
		JsonObject obj = req.params.getAsJsonObject();
		return Models.getDescriptor(obj);
	}

}
