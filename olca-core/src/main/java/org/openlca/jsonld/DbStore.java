package org.openlca.jsonld;

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
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DbStore implements JsonStoreReader, JsonStoreWriter {

	private final IDatabase db;

	private DbStore(IDatabase db) {
		this.db = db;
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
		if (path == null)
			return null;
		var file = new File(path);
		if (!file.isFile())
			return null;
		try {
			return Files.readAllBytes(file.toPath());
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to read file: " + file, e);
			return null;
		}
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
		T entity = Strings.notEmpty(id)
				? db.get(type, id)
				: null;

		if (entity != null) {
			reader.update(entity, json);
			 db.update(entity);
		} else {
			// add an ID, if not provided
			if (Strings.nullOrEmpty(id)) {
				Json.put(json, "@id", UUID.randomUUID().toString());
			}
			entity = reader.read(json);
			if (entity == null)
				return;
			db.insert(entity);
		}
	}

	@Override
	public void put(String path, JsonObject object) {
		JsonStoreWriter.super.put(path, object);
	}

	@Override
	public void putBin(ModelType type, String refId, String filename, byte[] data) {
		JsonStoreWriter.super.putBin(type, refId, filename, data);
	}

	@Override
	public void put(String path, byte[] data) {

	}


}
