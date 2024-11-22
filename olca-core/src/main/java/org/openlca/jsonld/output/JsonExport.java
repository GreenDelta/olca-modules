package org.openlca.jsonld.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.Daos;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Callback;
import org.openlca.core.model.Callback.Message;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.MemStore;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

	final JsonRefs dbRefs;
	private final Map<ModelType, Set<String>> visited = new EnumMap<>(ModelType.class);

	/// Exporting providers can lead to a stack overflow when calling write
	/// recursively. Thus, we need to queue them.
	private final ArrayDeque<WriteItem<?>> pQueue = new ArrayDeque<>();

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

	boolean hasVisited(ModelType type, String refId) {
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
			writeNext(e, null);
		}
		return Json.asRef(e);
	}

	JsonObject handleProvider(long pid) {
		if (pid == 0 || db == null || dbRefs == null)
			return null;
		var d = dbRefs.descriptorOf(ModelType.PROCESS, pid);
		if (d == null)
			return null;
		var ref = dbRefs.asRef(d);
		if (ref == null)
			return null;

		if (exportReferences
				&& exportProviders
				&& !hasVisited(ModelType.PROCESS, d.refId)) {
			var item = WriteItem.of(d);
			if (!pQueue.contains(item)) {
				pQueue.add(item);
			}
		}
		return ref;
	}

	public <T extends RootEntity> void write(T entity) {
		write(entity, null);
	}

	public <T extends RootEntity> void write(T entity, Callback cb) {
		if (entity == null)
			return;
		if (!exportProviders || !(entity instanceof Process)) {
			writeNext(entity, cb);
			return;
		}

		pQueue.add(WriteItem.of(entity));
		while (!pQueue.isEmpty()) {
			var next = pQueue.poll();
			if (!next.isValid())
				continue;
			writeNext(next.get(db), cb);
		}
	}

	private <T extends RootEntity> void writeNext(T entity, Callback cb) {

		// check the entity
		if (entity == null)
			return;
		var type = ModelType.of(entity);
		if (type == null || entity.refId == null) {
			warn(cb, "no refId; or type is unknown", entity);
			return;
		}

		// check visited state
		if (hasVisited(type, entity.refId))
			return;
		visited.computeIfAbsent(type, $ -> new HashSet<>())
				.add(entity.refId);
		if (skipLibraryData && Strings.notEmpty(entity.library)) {
			return;
		}

		try {

			// convert the entity to JSON
			JsonWriter<T> w = getWriter(entity);
			if (w == null) {
				warn(cb, "no writer found for type " + type, entity);
				return;
			}
			var obj = w.write(entity);

			// write it
			writer.put(type, obj);
			if (!skipExternalFiles) {
				writeExternalFiles(entity, type, cb);
			}
			if (cb != null) {
				cb.apply(Message.info("data set exported"), entity);
			}

		} catch (Exception e) {
			if (cb != null) {
				cb.apply(Message.error("failed to export data set", e), entity);
			}
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

	public <T extends RefEntity> JsonWriter<T> getWriter(T entity) {
		return Util.writerOf(entity, this);
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

	private record WriteItem<T extends RootEntity>(
			ModelType type, T entity, Descriptor descriptor
	) {

		static <T extends RootEntity> WriteItem<T> of(T entity) {
			var type = ModelType.of(entity);
			return new WriteItem<>(type, entity, null);
		}

		static <T extends RootEntity> WriteItem<T> of(Descriptor descriptor) {
			var type = descriptor.type;
			return new WriteItem<>(type, null, descriptor);
		}

		@SuppressWarnings("unchecked")
		public T get(IDatabase db) {
			if (entity != null)
				return entity;
			if (db == null || type == null || descriptor == null)
				return null;
			var e = db.get(type.getModelClass(), descriptor.id);
			return (T) e;
		}

		boolean isValid() {
			return type != null && (entity != null || descriptor != null);
		}
	}

}
