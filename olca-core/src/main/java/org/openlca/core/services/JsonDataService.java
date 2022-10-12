package org.openlca.core.services;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.ActorReader;
import org.openlca.jsonld.input.CurrencyReader;
import org.openlca.jsonld.input.DQSystemReader;
import org.openlca.jsonld.input.EntityReader;
import org.openlca.jsonld.input.EpdReader;
import org.openlca.jsonld.input.FlowPropertyReader;
import org.openlca.jsonld.input.FlowReader;
import org.openlca.jsonld.input.ImpactCategoryReader;
import org.openlca.jsonld.input.ImpactMethodReader;
import org.openlca.jsonld.input.LocationReader;
import org.openlca.jsonld.input.ParameterReader;
import org.openlca.jsonld.input.ProcessReader;
import org.openlca.jsonld.input.ProductSystemReader;
import org.openlca.jsonld.input.ProjectReader;
import org.openlca.jsonld.input.ResultReader;
import org.openlca.jsonld.input.SocialIndicatorReader;
import org.openlca.jsonld.input.SourceReader;
import org.openlca.jsonld.input.UnitGroupReader;
import org.openlca.jsonld.output.JsonRefs;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.jsonld.output.JsonWriter;
import org.openlca.util.Strings;

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
			var refs = JsonRefs.of(db);
			var descriptors = db.getDescriptors(type);
			for (var d : descriptors) {
				var ref = refs.asRef(d);
				array.add(ref);
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
		if (type == null || id == null)
			return Response.error("type or ID missing");
		try {
			var d = db.getDescriptor(type, id);
			if (d == null)
				return Response.empty();
			var ref = JsonRefs.of(db).asRef(d);
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

		EntityReader<T> reader = readerOf(type);
		if (reader == null)
			return Response.error("no reader for type '" + type + "' available");

		try {
			var id = Json.getString(json, "@id");
			T entity = Strings.notEmpty(id)
					? db.get(type, id)
					: null;

			if (entity != null) {
				reader.update(entity, json);
				entity = db.update(entity);
			} else {

				// add an ID, if not provided
				if (Strings.nullOrEmpty(id)) {
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
			var providers = ProcessTable.create(db).getProviders();
			var array = new JsonArray();
			var refs = JsonRefs.of(db);
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
			var providers = ProcessTable.create(db).getProviders(flow.id);
			var array = new JsonArray();
			var refs = JsonRefs.of(db);
			for (var p : providers) {
				array.add(JsonUtil.encodeTechFlow(p, refs));
			}
			return Response.of(array);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> EntityReader<T> readerOf(Class<T> type) {
		var resolver = new DbEntityResolver(db);
		if (Objects.equals(type, Actor.class))
			return (EntityReader<T>) new ActorReader(resolver);
		if (Objects.equals(type, Currency.class))
			return (EntityReader<T>) new CurrencyReader(resolver);
		if (Objects.equals(type, DQSystem.class))
			return (EntityReader<T>) new DQSystemReader(resolver);
		if (Objects.equals(type, Epd.class))
			return (EntityReader<T>) new EpdReader(resolver);
		if (Objects.equals(type, Flow.class))
			return (EntityReader<T>) new FlowReader(resolver);
		if (Objects.equals(type, FlowProperty.class))
			return (EntityReader<T>) new FlowPropertyReader(resolver);
		if (Objects.equals(type, ImpactCategory.class))
			return (EntityReader<T>) new ImpactCategoryReader(resolver);
		if (Objects.equals(type, ImpactMethod.class))
			return (EntityReader<T>) new ImpactMethodReader(resolver);
		if (Objects.equals(type, Location.class))
			return (EntityReader<T>) new LocationReader(resolver);
		if (Objects.equals(type, Parameter.class))
			return (EntityReader<T>) new ParameterReader(resolver);
		if (Objects.equals(type, Process.class))
			return (EntityReader<T>) new ProcessReader(resolver);
		if (Objects.equals(type, ProductSystem.class))
			return (EntityReader<T>) new ProductSystemReader(resolver);
		if (Objects.equals(type, Project.class))
			return (EntityReader<T>) new ProjectReader(resolver);
		if (Objects.equals(type, Result.class))
			return (EntityReader<T>) new ResultReader(resolver);
		if (Objects.equals(type, SocialIndicator.class))
			return (EntityReader<T>) new SocialIndicatorReader(resolver);
		if (Objects.equals(type, Source.class))
			return (EntityReader<T>) new SourceReader(resolver);
		if (Objects.equals(type, UnitGroup.class))
			return (EntityReader<T>) new UnitGroupReader(resolver);
		return null;
	}
}
