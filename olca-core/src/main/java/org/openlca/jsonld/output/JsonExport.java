package org.openlca.jsonld.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.Daos;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Callback;
import org.openlca.core.model.Callback.Message;
import org.openlca.core.model.Category;
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

import gnu.trove.set.hash.TLongHashSet;

/**
 * Writes entities to an entity store (e.g. a document or zip file). It also
 * writes the referenced entities to this store if they are not yet contained.
 */
public class JsonExport {

	final IDatabase db;
	final JsonStoreWriter writer;
	boolean exportReferences = true;
	boolean exportProviders = false;

	private final Map<ModelType, TLongHashSet> visited = new EnumMap<>(ModelType.class);
	final Refs refs;

	/**
	 * Creates an export without database. This can be useful to convert specific
	 * objects into JSON but some data may not be convertible with such an export
	 * (e.g. process links).
	 */
	public JsonExport(JsonStoreWriter writer) {
		this(null, writer);
	}

	public JsonExport(IDatabase db, JsonStoreWriter writer) {
		this.db = db;
		this.writer = Objects.requireNonNull(writer);
		this.refs = db != null
			? Refs.of(db)
			: null;
	}

	public JsonExport withDefaultProviders(boolean value) {
		exportProviders = value;
		return this;
	}

	public JsonExport withReferences(boolean value) {
		exportReferences = value;
		return this;
	}

	private void setVisited(RefEntity entity) {
		if (entity == null)
			return;
		var type = ModelType.of(entity);
		if (type == null)
			return;
		var set = visited.computeIfAbsent(type, k -> new TLongHashSet());
		set.add(entity.id);
	}

	private boolean hasVisited(ModelType type, long id) {
		var set = visited.get(type);
		return set != null && set.contains(id);
	}

	JsonObject handleRef(ModelType type, long id) {
		if (type == null || !type.isRoot() || db == null)
			return null;
		if (hasVisited(type, id) || !exportReferences)
			return refs.get(type, id);

		var dao = Daos.root(db, type);
		if (dao == null)
			return null;
		var entity = dao.getForId(id);
		if (entity == null)
			return null;
		write(entity);
		return Json.asRef(entity);
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

	public <T extends RefEntity> void write(T entity) {
		write(entity, null);
	}

	public <T extends RefEntity> void write(T entity, Callback cb) {
		if (entity == null)
			return;
		var type = ModelType.of(entity);
		if (type == null || entity.refId == null) {
			warn(cb, "no refId; or type is unknown", entity);
			return;
		}
		if (hasVisited(type, entity.id))
			return;
		setVisited(entity);
		Writer<T> w = getWriter(entity);
		if (w == null) {
			warn(cb, "no writer found for type " + type, entity);
			return;
		}
		try {
			var obj = w.write(entity);
			writer.put(type, obj);
			writeExternalFiles(entity, type, cb);
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
		RefEntity entity, ModelType type, Callback cb) {
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
		Writer<T> writer = exp.getWriter(entity);
		return writer.write(entity);
	}

	@SuppressWarnings("unchecked")
	private <T extends RefEntity> Writer<T> getWriter(T entity) {
		if (entity == null)
			return null;
		if (entity instanceof Actor)
			return (Writer<T>) new ActorWriter(this);
		if (entity instanceof Category)
			return (Writer<T>) new CategoryWriter(this);
		if (entity instanceof Currency)
			return (Writer<T>) new CurrencyWriter(this);
		if (entity instanceof Epd)
			return (Writer<T>) new EpdWriter(this);
		if (entity instanceof FlowProperty)
			return (Writer<T>) new FlowPropertyWriter(this);
		if (entity instanceof Flow)
			return (Writer<T>) new FlowWriter(this);
		if (entity instanceof ImpactCategory)
			return (Writer<T>) new ImpactCategoryWriter(this);
		if (entity instanceof ImpactMethod)
			return (Writer<T>) new ImpactMethodWriter(this);
		if (entity instanceof Location)
			return (Writer<T>) new LocationWriter(this);
		if (entity instanceof Parameter)
			return (Writer<T>) new ParameterWriter(this);
		if (entity instanceof Process)
			return (Writer<T>) new ProcessWriter(this);
		if (entity instanceof Result)
			return (Writer<T>) new ResultWriter(this);
		if (entity instanceof Source)
			return (Writer<T>) new SourceWriter(this);
		if (entity instanceof UnitGroup)
			return (Writer<T>) new UnitGroupWriter(this);
		if (entity instanceof SocialIndicator)
			return (Writer<T>) new SocialIndicatorWriter(this);
		if (entity instanceof ProductSystem)
			return (Writer<T>) new ProductSystemWriter(this);
		if (entity instanceof Project)
			return (Writer<T>) new ProjectWriter(this);
		if (entity instanceof DQSystem)
			return (Writer<T>) new DQSystemWriter(this);
		if (entity instanceof Unit)
			return (Writer<T>) new UnitWriter(this);
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
