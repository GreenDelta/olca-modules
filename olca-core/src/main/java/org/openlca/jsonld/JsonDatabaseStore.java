package org.openlca.jsonld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.persistence.Table;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.input.EntityReader;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Dirs;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


/**
 * An implementation of the JSON reader and writer interface that directly works
 * on a database. It is not the recommended way to use this for importing and
 * exporting data sets - use the specific import and export classes for this -
 * but it can be used in specific use cases e.g. when transferring a
 * foreground-model from one database to another.
 */
public class JsonDatabaseStore implements JsonStoreReader, JsonStoreWriter {

	private final IDatabase db;

	private JsonDatabaseStore(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static JsonDatabaseStore of(IDatabase db) {
		return new JsonDatabaseStore(db);
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		if (type == null)
			return List.of();
		var table = type.getModelClass().getAnnotation(Table.class);
		if (table == null)
			return List.of();
		var query = "select ref_id from " + table.name();
		try {
			var ids = new ArrayList<String>();
			NativeSql.on(db).query(query, r -> {
				ids.add(r.getString(1));
				return true;
			});
			return ids;
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to query ref. ids", e);
			return List.of();
		}
	}

	@Override
	public List<String> getFiles(String dir) {
		if (dir == null)
			return List.of();
		var root = db.getFileStorageLocation();
		if (root == null || !root.exists())
			return List.of();
		var sub = new File(root, dir);
		return listFilesOf(sub);
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		var root = db.getFileStorageLocation();
		if (root == null || !root.isDirectory())
			return List.of();
		var store = new FileStore(root);
		var dir = store.getFolder(type, refId);
		return listFilesOf(dir);
	}

	private static List<String> listFilesOf(File dir) {
		if (dir == null || !dir.isDirectory())
			return List.of();
		var files = dir.listFiles();
		if (files == null)
			return List.of();
		var paths = new ArrayList<String>();
		for (var f : files) {
			if (f.isDirectory())
				continue;
			paths.add(f.getAbsolutePath());
		}
		return paths;
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (type == null || refId == null)
			return null;
		var entity = db.get(type.getModelClass(), refId);
		if (entity == null)
			return null;
		return new JsonExport(db, new MemStore())
				.withReferences(false)
				.getWriter(entity)
				.write(entity);
	}

	@Override
	public byte[] getBytes(String path) {
		if (Strings.isBlank(path))
			return null;

		// 1. read a possible file
		var file = new File(path);
		if (file.isFile()) {
			try {
				return Files.readAllBytes(file.toPath());
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("failed to read file: " + file, e);
				return null;
			}
		}

		// 2. read a possible model
		var info = ModelInfo.parseFrom(path).orElse(null);
		if (info == null)
			return null;
		var json = get(info.type, info.refId);
		return json != null
				? new Gson().toJson(json).getBytes(StandardCharsets.UTF_8)
				: null;
	}

	@Override
	public void put(ModelType type, JsonObject json) {
		if (type == null || json == null)
			return;
		put(type.getModelClass(), json);
	}

	private <T extends RootEntity> void put(Class<T> type, JsonObject json) {
		var resolver = DbEntityResolver.of(db)
				.withCategoryCreation(true);
		var reader = EntityReader.of(type, resolver);
		if (reader == null) {
			LoggerFactory.getLogger(getClass())
					.error("no registered entity reader for type {}", type);
			return;
		}

		var id = Json.getString(json, "@id");
		T entity = Strings.isNotBlank(id)
				? db.get(type, id)
				: null;

		if (entity != null) {
			reader.update(entity, json);
			db.update(entity);
		} else {
			// add an ID, if not provided
			if (Strings.isBlank(id)) {
				Json.put(json, "@id", UUID.randomUUID().toString());
			}
			entity = reader.read(json);
			if (entity == null)
				return;
			db.insert(entity);
		}
	}

	@Override
	public void put(String path, JsonObject obj) {
		if (path == null || obj == null)
			return;
		var info = ModelInfo.parseFrom(path).orElse(null);
		if (info == null) {
			LoggerFactory.getLogger(getClass())
					.warn("could not determine model-info from path '{}'", path);
			return;
		}
		info.put(this, obj);
	}

	@Override
	public void putBin(ModelType type, String refId, String fileName, byte[] data) {
		if (type == null || refId == null || fileName == null || data == null)
			return;
		var root = db.getFileStorageLocation();
		if (root == null || !root.isDirectory())
			return;
		var store = new FileStore(root);
		var dir = store.getFolder(type, refId);
		try {
			Dirs.createIfAbsent(dir);
			var file = new File(dir, fileName);
			Files.write(file.toPath(), data);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to write to file: " + fileName, e);
		}
	}

	@Override
	public void put(String path, byte[] data) {

		// try to insert it as model
		var info = ModelInfo.parseFrom(path).orElse(null);
		if (info != null) {
			try (var stream = new ByteArrayInputStream(data);
					 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				var obj = new Gson().fromJson(reader, JsonObject.class);
				info.put(this, obj);
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass())
						.error("failed to parse JSON object", e);
			}
		}

		// try to insert it as file
		var root = db.getFileStorageLocation();
		if (root == null || !root.isDirectory())
			return;
		var file = new File(root, path);
		try {
			Dirs.createIfAbsent(file.getParentFile());
			Files.write(file.toPath(), data);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed write file " + file, e);
		}
	}

	private record ModelInfo(ModelType type, String refId) {

		static Optional<ModelInfo> parseFrom(String path) {
			if (Strings.isBlank(path) || !path.endsWith(".json"))
				return Optional.empty();
			var parts = path.substring(0, path.length() - 5).split("/");
			if (parts.length != 2)
				return Optional.empty();
			var type = ModelPath.typeOf(parts[0]).orElse(null);
			return type != null
					? Optional.of(new ModelInfo(type, parts[1]))
					: Optional.empty();
		}

		void put(JsonDatabaseStore store, JsonObject obj) {
			if (Json.getString(obj, "@id") == null) {
				Json.put(obj, "@id", refId);
			}
			store.put(type.getModelClass(), obj);
		}
	}
}
