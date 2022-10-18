package org.openlca.jsonld.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.Daos;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Callback;
import org.openlca.core.model.Callback.Message;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.MemStore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.openlca.util.Strings;

/**
 * Writes entities to an entity store (e.g. a document or zip file). It also
 * writes the referenced entities to this store if they are not yet contained.
 */
public class JsonExport {

	final IDatabase db;
	final JsonStoreWriter writer;
	boolean exportReferences = true;
	boolean skipLibraryData = true;
	boolean exportProviders = false;
	boolean skipExternalFiles = false;

	private final Map<ModelType, Set<String>> visited = new EnumMap<>(ModelType.class);
	final JsonRefs dbRefs;

	/**
	 * Creates an export without database. This can be useful to convert
	 * specific objects into JSON but some data may not be convertible with such
	 * an export (e.g. process links).
	 */
	public JsonExport(JsonStoreWriter writer) {
		this(null, writer);
	}

	public JsonExport(IDatabase db, JsonStoreWriter writer) {
		this.db = db;
		this.writer = Objects.requireNonNull(writer);
		this.dbRefs = db != null
				? JsonRefs.of(db)
				: null;
	}

	/**
	 * Configures whether default providers of product inputs or waste outputs
	 * should be exported or not. This is set to {@code false} by default. Note
	 * that if this is enabled, a large amount of processes could be exported
	 * recursively if they are connected by their default providers.
	 */
	public JsonExport withDefaultProviders(boolean b) {
		exportProviders = b;
		return this;
	}

	public JsonExport withReferences(boolean b) {
		exportReferences = b;
		return this;
	}

	public JsonExport skipLibraryData(boolean b) {
		skipLibraryData = b;
		return this;
	}

	public JsonExport skipExternalFiles(boolean b) {
		skipExternalFiles = b;
		return this;
	}

	private void setVisited(RootEntity entity) {
		if (entity == null || entity.refId == null)
			return;
		var type = ModelType.of(entity);
		if (type == null)
			return;
		var set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(entity.refId);
	}

	private boolean hasVisited(ModelType type, String refId) {
		var set = visited.get(type);
		return set != null && set.contains(refId);
	}

	/**
	 * Creates a short reference for the entity with the given type and ID. This
	 * method should be only used when nothing more than the ID and type of the
	 * thing are available (e.g. for default providers in exchanges or processes
	 * in process links).
	 */
	JsonObject handleRef(ModelType type, long id) {
		if (type == null || dbRefs == null)
			return null;
		var d = dbRefs.descriptorOf(type, id);
		if (d == null)
			return null;
		if (hasVisited(type, d.refId) || !exportReferences)
			return dbRefs.asRef(d);
		var dao = Daos.root(db, type);
		return dao != null
				? handleRef(dao.getForId(id))
				: null;
	}

	JsonArray handleRefs(List<? extends RootEntity> list) {
		if (list == null || list.isEmpty())
			return null;
		var array = new JsonArray();
		list.stream()
				.map(this::handleRef)
				.filter(Objects::nonNull)
				.forEach(array::add);
		return array;
	}

	JsonObject handleRef(RootEntity e) {
		if (e == null)
			return null;
		if (exportReferences) {
			write(e);
		}
		return Json.asRef(e);
	}

	public <T extends RootEntity> void write(T entity) {
		write(entity, null);
	}

	public <T extends RootEntity> void write(T entity, Callback cb) {
		if (entity == null)
			return;
		var type = ModelType.of(entity);
		if (type == null || entity.refId == null) {
			warn(cb, "no refId; or type is unknown", entity);
			return;
		}
		if (hasVisited(type, entity.refId))
			return;
		setVisited(entity);

		// check skip library data
		if (skipLibraryData && Strings.notEmpty(entity.library)) {
			return;
		}

		JsonWriter<T> w = getWriter(entity);
		if (w == null) {
			warn(cb, "no writer found for type " + type, entity);
			return;
		}
		try {
			var obj = w.write(entity);
			writer.put(type, obj);
			if (!skipExternalFiles) {
				writeExternalFiles(entity, type, cb);
			}
			if (cb != null)
				cb.apply(Message.info("data set exported"), entity);
		} catch (Exception e) {
			if (cb != null)
				cb.apply(Message.error("failed to export data set", e), entity);
		}
	}

	private void warn(Callback cb, String message, RefEntity entity) {
		if (cb == null)
			return;
		cb.apply(Message.warn(message), entity);
	}

	private void writeExternalFiles(
			RootEntity entity, ModelType type, Callback cb) {
		if (entity == null || db == null
				|| db.getFileStorageLocation() == null
				|| writer == null)
			return;
		FileStore fs = new FileStore(db.getFileStorageLocation());
		File dir = fs.getFolder(entity);
		if (dir == null || !dir.exists())
			return;
		try {
			Path dbDir = dir.toPath();
			Copy copy = new Copy(entity.refId, type, dbDir);
			Files.walkFileTree(dir.toPath(), copy);
		} catch (Exception e) {
			cb.apply(Message.error("failed to copy external files", e), entity);
		}
	}

	public static <T extends RefEntity> JsonObject toJson(
			T entity, IDatabase db) {
		if (entity == null)
			return new JsonObject();
		var exp = new JsonExport(db, new MemStore())
				.withReferences(false);
		var writer = exp.getWriter(entity);
		return writer.write(entity);
	}

	public static <T extends RefEntity> JsonObject toJson(T entity) {
		if (entity == null)
			return new JsonObject();
		var exp = new JsonExport(null, new MemStore())
				.withReferences(false);
		JsonWriter<T> writer = exp.getWriter(entity);
		return writer.write(entity);
	}

	@SuppressWarnings("unchecked")
	public <T extends RefEntity> JsonWriter<T> getWriter(T entity) {
		if (entity == null)
			return null;
		if (entity instanceof Actor)
			return (JsonWriter<T>) new ActorWriter();
		if (entity instanceof Currency)
			return (JsonWriter<T>) new CurrencyWriter(this);
		if (entity instanceof Epd)
			return (JsonWriter<T>) new EpdWriter(this);
		if (entity instanceof FlowProperty)
			return (JsonWriter<T>) new FlowPropertyWriter(this);
		if (entity instanceof Flow)
			return (JsonWriter<T>) new FlowWriter(this);
		if (entity instanceof ImpactCategory)
			return (JsonWriter<T>) new ImpactCategoryWriter(this);
		if (entity instanceof ImpactMethod)
			return (JsonWriter<T>) new ImpactMethodWriter(this);
		if (entity instanceof Location)
			return (JsonWriter<T>) new LocationWriter(this);
		if (entity instanceof Parameter)
			return (JsonWriter<T>) new ParameterWriter(this);
		if (entity instanceof Process)
			return (JsonWriter<T>) new ProcessWriter(this);
		if (entity instanceof Result)
			return (JsonWriter<T>) new ResultWriter(this);
		if (entity instanceof Source)
			return (JsonWriter<T>) new SourceWriter(this);
		if (entity instanceof UnitGroup)
			return (JsonWriter<T>) new UnitGroupWriter(this);
		if (entity instanceof SocialIndicator)
			return (JsonWriter<T>) new SocialIndicatorWriter(this);
		if (entity instanceof ProductSystem)
			return (JsonWriter<T>) new ProductSystemWriter(this);
		if (entity instanceof Project)
			return (JsonWriter<T>) new ProjectWriter(this);
		if (entity instanceof DQSystem)
			return (JsonWriter<T>) new DQSystemWriter(this);
		if (entity instanceof Unit)
			return (JsonWriter<T>) new UnitWriter(this);
		return null;
	}

	private class Copy extends SimpleFileVisitor<Path> {

		private final String refId;
		private final ModelType type;
		private final Path dbDir;

		Copy(String refId, ModelType type, Path dbDir) {
			this.refId = refId;
			this.dbDir = dbDir;
			this.type = type;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			String path = dbDir.relativize(file).toString().replace('\\', '/');
			byte[] data = Files.readAllBytes(file);
			writer.putBin(type, refId, path, data);
			return FileVisitResult.CONTINUE;
		}

	}
}
