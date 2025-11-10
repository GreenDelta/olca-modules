package org.openlca.core.services;

import java.util.UUID;
import java.util.function.Supplier;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.matrix.ProductSystemBuilder;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ParameterizedEntity;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.EntityReader;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.jsonld.output.JsonRefs;
import org.openlca.jsonld.output.JsonWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record JsonDataService(IDatabase db) {

	/**
	 * Get the data set for the given type and ID. Returns an empty response if
	 * no such data set exists.
	 */
	public Response<JsonObject> get(Class<? extends RootEntity> type, String id) {
		return type == null || id == null
				? Response.error("type or ID missing")
				: fetchEntity(() -> db.get(type, id));
	}

	public Response<JsonObject> getForName(
			Class<? extends RootEntity> type, String name) {
		return type == null || name == null
				? Response.error("type or name missing")
				: fetchEntity(() -> db.getForName(type, name));
	}

	private Response<JsonObject> fetchEntity(Supplier<? extends RootEntity> fn) {
		try {
			var entity = fn.get();
			if (entity == null)
				return Response.empty();
			var json = new JsonExport(db, new MemStore())
					.withLibraryFields(true)
					.withReferences(false)
					.getWriter(entity)
					.write(entity);
			return Response.of(json);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	/**
	 * Get all data sets of the given type from the database. Note that this
	 * function can produce a huge amount of data depending on the requested
	 * type and database. So it needs to be carefully checked before exposing
	 * this function to a service.
	 */
	public <T extends RootEntity> Response<JsonArray> getAll(Class<T> type) {
		if (type == null)
			return Response.error("type is missing");
		try {
			var export = new JsonExport(db, new MemStore())
					.withLibraryFields(true)
					.withReferences(false);
			JsonWriter<T> writer = null;
			var array = new JsonArray();
			for (var entity : db.getAll(type)) {
				if (writer == null) {
					writer = export.getWriter(entity);
				}
				array.add(writer.write(entity));
			}
			return Response.of(array);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonArray> getDescriptors(Class<? extends RootEntity> type) {
		if (type == null)
			return Response.error("type missing");
		try {
			var array = new JsonArray();
			var refs = refs();
			var descriptors = db.getDescriptors(type);
			for (var d : descriptors) {
				array.add(refs.asRef(d));
			}
			return Response.of(array);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	/**
	 * Get the descriptor for the given type and ID. Returns an empty response if
	 * no such descriptor exists.
	 */
	public Response<JsonObject> getDescriptor(
			Class<? extends RootEntity> type, String id) {
		if (type == null || Strings.isBlank(id))
			return Response.error("type or ID missing");
		try {
			var d = db.getDescriptor(type, id);
			if (d == null)
				return Response.empty();
			return Response.of(refs().asRef(d));
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonObject> getDescriptorForName(
			Class<? extends RootEntity> type, String name) {
		if (type == null || Strings.isBlank(name))
			return Response.error("type or name missing");
		try {
			var d = db.getForName(type, name);
			if (d == null)
				return Response.empty();
			var ref = Json.asRef(d);
			if (d.isFromLibrary()) {
				Json.put(ref, "library", d.library);
			}
			return Response.of(ref);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	/**
	 * Same method as {@link #put(Class, JsonObject)} but it tries to read the
	 * data set type from the {@code @type} attribute of the given Json object.
	 */
	public Response<JsonObject> put(JsonObject json) {
		if (json == null)
			return Response.error("no data set provided");
		var type = JsonRef.typeOf(json);
		if (type == null)
			return Response.error("Json object is not annotated with a known type");
		return put(type.getModelClass(), json);
	}

	/**
	 * Inserts or updates the given data set in the database depending on if it is
	 * already present or not. An ID does not have to be provided; if not
	 * available it will be generated.
	 *
	 * @param type the type of the entity
	 * @param json the data set
	 * @return the descriptor of the inserted or updated entity or a
	 * corresponding error.
	 */
	public <T extends RootEntity> Response<JsonObject> put(
			Class<T> type, JsonObject json) {
		if (type == null || json == null)
			return Response.error("no type or data set provided");

		var resolver = DbEntityResolver.of(db)
				.withCategoryCreation(true);
		EntityReader<T> reader = EntityReader.of(type, resolver);
		if (reader == null)
			return Response.error("no reader for type '" + type + "' available");

		try {
			var id = Json.getString(json, "@id");
			T entity = Strings.isNotBlank(id)
					? db.get(type, id)
					: null;

			if (entity != null) {
				if (entity.isFromLibrary())
					return Response.error("library data cannot be updated");

				reader.update(entity, json);
				entity = db.update(entity);

			} else {

				// add an ID, if not provided
				if (Strings.isBlank(id)) {
					Json.put(json, "@id", UUID.randomUUID().toString());
				}

				entity = reader.read(json);
				if (entity == null)
					return Response.error("failed to read Json");
				entity = db.insert(entity);
			}

			var ref = Json.asRef(entity);
			return Response.of(ref);

		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonObject> createProductSystem(
			String processId, JsonObject jsonConfig) {
		var process = db.get(Process.class, processId);
		if (process == null)
			return Response.error("process does not exist: id=" + processId);
		if (process.quantitativeReference == null)
			return Response.error("process does not have a quantitative reference");
		var system = db.insert(ProductSystem.of(process));
		var config = JsonUtil.linkingConfigOf(jsonConfig);
		var builder = new ProductSystemBuilder(db, config);
		builder.autoComplete(system);
		system = ProductSystemBuilder.update(db, system);
		var ref = Json.asRef(system);
		return Response.of(ref);
	}

	/**
	 * Deletes the specified data set from the database. Returns the descriptor
	 * of the data set if it was successfully deleted, an empty response if no
	 * such entity was present, or an error if the deletion failed.
	 */
	public <T extends RootEntity> Response<JsonObject> delete(
			Class<T> type, String id) {
		if (type == null || id == null)
			return Response.error("no type of ID provided");
		try {
			var entity = db.get(type, id);
			if (entity == null)
				return Response.empty();
			if (entity.isFromLibrary())
				return Response.error("library data cannot be deleted");
			var ref = Json.asRef(entity);
			db.delete(entity);
			return Response.of(ref);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	/**
	 * Get all process-flow pairs that can be linked to product inputs or waste
	 * outputs from the database.
	 */
	public Response<JsonArray> getProviders() {
		try {
			var providers = ProviderMap.create(db).getTechFlows();
			var array = new JsonArray();
			var refs = refs();
			for (var p : providers) {
				array.add(JsonUtil.encodeTechFlow(p, refs));
			}
			return Response.of(array);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonArray> getProvidersOfFlow(String flowId) {
		if (flowId == null)
			return Response.error("no flow ID provided");
		try {
			var flow = db.getDescriptor(Flow.class, flowId);
			if (flow == null)
				return Response.empty();
			var providers = ProviderMap.create(db).getProvidersOf(flow.id);
			var array = new JsonArray();
			var refs = refs();
			for (var p : providers) {
				array.add(JsonUtil.encodeTechFlow(p, refs));
			}
			return Response.of(array);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public <T extends RootEntity> Response<JsonArray> getParametersOf(
			Class<T> type, String id) {
		if (type == null || id == null)
			return Response.error("no type or ID provided");
		var entity = db.get(type, id);

		return switch (entity) {

			case null -> Response.empty();

			case ParameterizedEntity pe -> {
				var exp = new JsonExport(db, new MemStore())
						.withReferences(false);
				var array = JsonParameters.of(exp, pe);
				yield Response.of(array);
			}

			case ProductSystem sys -> {
				var array = JsonParameters.of(db, sys);
				yield Response.of(array);
			}

			default -> Response.error(
					"unsupported parameter container: type=" + type + " id=" + id);
		};
	}

	private JsonRefs refs() {
		return JsonRefs.of(db).withLibraryFields(true);
	}
}
