package org.openlca.ipc.handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.function.BiFunction;

import org.openlca.commons.Strings;
import org.openlca.core.database.FileStore;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonRef;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Dirs;

import com.google.gson.JsonObject;

public class DataHandler {

	private final JsonDataService service;

	public DataHandler(HandlerContext context) {
		this.service = new JsonDataService(context.db());
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
			if (Strings.isNotBlank(id))
				return Responses.of(service.get(type.getModelClass(), id), req);
			var name = Json.getString(json, "name");
			if (Strings.isNotBlank(name))
				return Responses.of(service.getForName(type.getModelClass(), name), req);
			return Responses.badRequest("no id or name provided", req);
		});
	}

	@Rpc("data/get/descriptor")
	public RpcResponse getDescriptor(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			var clazz = type.getModelClass();
			var id = JsonRef.idOf(json);
			if (Strings.isNotBlank(id)) {
				var resp = service.getDescriptor(clazz, id);
				return Responses.of(resp, req);
			}
			var name = Json.getString(json, "name");
			if (Strings.isNotBlank(name)) {
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

	@Rpc("data/put/source-file")
	public RpcResponse putSourceFile(RpcRequest req) {
		var r = req.requireJsonObject();
		if (r.isError())
			return Responses.badRequest(r.error(), req);
		var obj = r.value();

		// get the source
		var sourceId = Json.getRefId(obj, "source");
		if (sourceId == null) {
			return Responses.badRequest(
				"No valid reference to a source provided", req);
		}
		var source = service.db().get(Source.class, sourceId);
		if (source == null) {
			return Responses.badRequest(
				"The source does not exist: " + sourceId, req);
		}

		// get the file information
		var fileObj = Json.getObject(obj, "file");
		if (fileObj == null) {
			return Responses.badRequest(
				"No file information provided", req);
		}
		var fileName = Json.getString(fileObj, "name");
		var base64 = Json.getString(fileObj, "content");
		if (Strings.isBlank(fileName) || Strings.isBlank(base64)) {
			return Responses.badRequest(
				"File name or content is missing", req);
		}

		// open the file storage of the database
		var store = FileStore.of(service.db());
		if (store.isError()) {
			return Responses.internalServerError(
				"Failed to access file storage of database: " + store.error(), req);
		}

		// copy the file to the database folder
		try {
			var bytes = Base64.getDecoder().decode(base64);
			var dir = store.value().getFolder(source);
			Dirs.createIfAbsent(dir);
			var file = new File(dir, fileName);
			var input = new ByteArrayInputStream(bytes);
			Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			return Responses.internalServerError(
				"Failed to store file in database: " + e.getMessage(), req);
		}

		// update the source with the file name
		source.externalFile = fileName;
		Version.incUpdate(source);
		service.db().update(source);
		return Responses.ok(req);
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
		if (Strings.isBlank(flowId)) {
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

	@Rpc("data/create/system")
	public RpcResponse createProductSystem(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("no parameters given", req);
		var obj = req.params.getAsJsonObject();
		var processId = Json.getRefId(obj, "process");
		var config = Json.getObject(obj, "config");
		var resp = service.createProductSystem(processId, config);
		return Responses.of(resp, req);
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
