package org.openlca.ipc.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.matrix.linking.ProviderLinking;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.services.JsonDataService;
import org.openlca.core.services.JsonRef;
import org.openlca.core.services.Response;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataHandler {

	private final JsonDataService service;
	private final IDatabase db;

	public DataHandler(HandlerContext context) {
		this.service = new JsonDataService(context.db());
		this.db = context.db();
	}

	@Rpc("data/get/descriptors")
	public RpcResponse getDescriptors(RpcRequest req) {
		return withTypedParam(req, (json, type) -> {
			// Check if query parameter is provided
			var query = Json.getString(json, "query");
			if (Strings.notEmpty(query)) {
				// Use search functionality if query is provided
				var searchResults = searchDescriptors(type, query);
				var array = new JsonArray();
				var refs = JsonRefs.of(db).withLibraryFields(true);
				for (var descriptor : searchResults) {
					if (descriptor instanceof RootDescriptor rootDesc) {
						array.add(refs.asRef(rootDesc));
					}
				}
				return Responses.of(Response.of(array), req);
			}
			
			// Original functionality - get all descriptors
			var resp = service.getDescriptors(type.getModelClass());
			return Responses.of(resp, req);
		});
	}

	@Rpc("data/search")
	public RpcResponse search(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject()) {
			return Responses.invalidParams("no parameters given", req);
		}
		
		var obj = req.params.getAsJsonObject();
		var query = Json.getString(obj, "query");
		if (Strings.nullOrEmpty(query)) {
			return Responses.invalidParams("query parameter is required", req);
		}
		
		// Get model type if specified
		ModelType typeFilter = null;
		var typeStr = Json.getString(obj, "type");
		if (Strings.notEmpty(typeStr)) {
			try {
				typeFilter = ModelType.valueOf(typeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				return Responses.invalidParams("invalid model type: " + typeStr, req);
			}
		}
		
		var searchResults = searchDescriptors(typeFilter, query);
		var array = new JsonArray();
		var refs = JsonRefs.of(db).withLibraryFields(true);
		for (var descriptor : searchResults) {
			if (descriptor instanceof RootDescriptor rootDesc) {
				array.add(refs.asRef(rootDesc));
			}
		}
		return Responses.of(Response.of(array), req);
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

	@Rpc("data/update/system/links")
	public RpcResponse updateProductSystemLinks(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject()) {
			return Responses.invalidParams("no parameters given", req);
		}

		var obj = req.params.getAsJsonObject();
		var systemId = Json.getLong(obj, "systemId", 0L);
		if (systemId <= 0) {
			return Responses.invalidParams("systemId must be a positive number", req);
		}

		try {
			// Parse parameters with defaults
			var providerLinking = parseProviderLinking(Json.getString(obj, "providerLinking"));
			var preferredType = parseProcessType(Json.getString(obj, "preferredType"));
			var keepExisting = parseBoolean(Json.getString(obj, "keepExisting"), true);

			// Load the product system
			var dao = new ProductSystemDao(db);
			var system = dao.getForId(systemId);
			if (system == null) {
				return Responses.badRequest("Product system with ID " + systemId + " not found", req);
			}

			// Create linking configuration
			var config = new LinkingConfig()
					.providerLinking(providerLinking)
					.preferredType(mapProcessType(preferredType));

			// Create builder and update the system
			var builder = new ProductSystemBuilder(db, config);
			
			// If not keeping existing, clear current links
			if (!keepExisting) {
				system.processLinks.clear();
				system.processes.clear();
			}
			
			// Auto-complete the system with new links
			builder.autoComplete(system);
			
			// Save the updated system
			ProductSystemBuilder.update(db, system);

			// Get the updated system as JSON
			var response = service.get(ProductSystem.class, String.valueOf(systemId));
			return Responses.of(response, req);

		} catch (Exception e) {
			return Responses.error(500, "Failed to update system links: " + e.getMessage(), req);
		}
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

	private List<Descriptor> searchDescriptors(ModelType typeFilter, String query) {
		var search = new Search(db, query, typeFilter);
		return search.run().toList();
	}

	/**
	 * Internal search implementation similar to olca-app
	 */
	private static class Search {
		private final IDatabase database;
		private final ModelType typeFilter;
		private final String[] terms;

		Search(IDatabase database, String query, ModelType typeFilter) {
			this.database = database;
			this.typeFilter = typeFilter;
			var rawTerm = query == null ? "" : query.toLowerCase().trim();
			terms = Arrays.stream(rawTerm.split(" "))
				.filter(s -> !s.isBlank())
				.toArray(String[]::new);
		}

		Stream<Descriptor> run() {
			return terms == null || terms.length == 0
				? Stream.empty()
				: Arrays.stream(types())
				.flatMap(type -> allOf(type).stream())
				.map(d -> new SearchResult(d, match(d)))
				.filter(result -> result.match != null)
				.sorted((r1, r2) -> compare(r1.match, r2.match))
				.map(r -> r.descriptor);
		}

		private ModelType[] types() {
			if (typeFilter != null)
				return new ModelType[]{typeFilter};
			return new ModelType[]{
				ModelType.CATEGORY,
				ModelType.PROJECT,
				ModelType.PRODUCT_SYSTEM,
				ModelType.IMPACT_METHOD,
				ModelType.IMPACT_CATEGORY,
				ModelType.PROCESS,
				ModelType.FLOW,
				ModelType.SOCIAL_INDICATOR,
				ModelType.PARAMETER,
				ModelType.FLOW_PROPERTY,
				ModelType.UNIT_GROUP,
				ModelType.CURRENCY,
				ModelType.ACTOR,
				ModelType.SOURCE,
				ModelType.LOCATION,
				ModelType.DQ_SYSTEM
			};
		}

		private List<? extends Descriptor> allOf(ModelType type) {
			if (type == ModelType.PARAMETER)
				return new ParameterDao(database).getGlobalDescriptors();
			var dao = Daos.root(database, type);
			return dao == null
				? Collections.emptyList()
				: dao.getDescriptors();
		}

		private int[] match(Descriptor d) {
			if (terms.length == 1
				&& d.refId != null
				&& d.refId.equalsIgnoreCase(terms[0])) {
				return new int[0];
			}
			var name = d.name;
			if (name == null)
				return null;
			var feed = name.toLowerCase();
			var match = new int[terms.length];
			var hasMatch = false;
			for (int i = 0; i < terms.length; i++) {
				int pos = feed.indexOf(terms[i]);
				if (pos >= 0) {
					hasMatch = true;
				}
				match[i] = pos;
			}
			return hasMatch ? match : null;
		}

		private int compare(int[] match1, int[] match2) {
			if (match1 == null || match2 == null)
				return 0;
			int n = Math.min(match1.length, match2.length);
			for (int i = 0; i < n; i++) {
				int pos1 = match1[i];
				int pos2 = match2[i];
				if (pos1 < 0 && pos2 < 0)
					continue;
				if (pos2 < 0)
					return -1;
				if (pos1 < 0)
					return 1;
				int diff = pos1 - pos2;
				if (diff != 0)
					return diff;
			}
			return 0;
		}

		private record SearchResult(Descriptor descriptor, int[] match) {}
	}

	private static LinkingConfig.PreferredType mapProcessType(ProcessType processType) {
		if (processType == null) {
			return LinkingConfig.PreferredType.SYSTEM_PROCESS;
		}
		return switch (processType) {
			case UNIT_PROCESS -> LinkingConfig.PreferredType.UNIT_PROCESS;
			case LCI_RESULT -> LinkingConfig.PreferredType.SYSTEM_PROCESS;
			default -> LinkingConfig.PreferredType.SYSTEM_PROCESS;
		};
	}

	private static ProviderLinking parseProviderLinking(String value) {
		if (Strings.nullOrEmpty(value)) {
			return ProviderLinking.PREFER_DEFAULTS;
		}
		try {
			return ProviderLinking.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ProviderLinking.PREFER_DEFAULTS;
		}
	}

	private static ProcessType parseProcessType(String value) {
		if (Strings.nullOrEmpty(value)) {
			return ProcessType.LCI_RESULT;
		}
		try {
			return ProcessType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ProcessType.LCI_RESULT;
		}
	}

	private static boolean parseBoolean(String value, boolean defaultValue) {
		if (Strings.nullOrEmpty(value)) {
			return defaultValue;
		}
		return "true".equalsIgnoreCase(value) || "1".equals(value);
	}
}
