package org.openlca.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

public class ObjectIdStore {

	private final File file;
	private Map<String, byte[]> workspace = new HashMap<>();
	private Map<String, byte[]> head = new HashMap<>();

	private ObjectIdStore(File storeFile) {
		this.file = storeFile;
	}

	public static ObjectIdStore fromFile(File storeFile) throws IOException {
		var store = new ObjectIdStore(storeFile);
		store.load();
		return store;
	}

	public static ObjectIdStore inMemory() {
		return new ObjectIdStore(null);
	}

	@SuppressWarnings("unchecked")
	private void load() throws IOException {
		if (file == null || !file.exists())
			return;
		try (var fis = new FileInputStream(file);
				var ois = new ObjectInputStream(fis)) {
			var stores = (List<HashMap<String, byte[]>>) ois.readObject();
			workspace = stores.get(0);
			head = stores.get(1);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public void save() throws IOException {
		if (file == null)
			return;
		if (!file.getParentFile().exists()) {
			Files.createDirectories(file.getParentFile().toPath());
		}
		try (var fos = new FileOutputStream(file);
				var oos = new ObjectOutputStream(fos)) {
			oos.writeObject(Arrays.asList(workspace, head));
		}
	}

	public boolean has(ModelType type) {
		var path = getPath(type, null, null);
		return has(path);
	}

	public boolean has(RootEntity e) {
		var path = getPath(e);
		return has(path);
	}

	public boolean has(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		return has(path);
	}

	public boolean has(String path) {
		return workspace.containsKey(path);
	}

	public byte[] getRawRoot() {
		return getRaw("");
	}

	public byte[] getRaw(ModelType type) {
		var path = getPath(type);
		return getRaw(path);
	}

	public byte[] getRaw(RootEntity e) {
		var path = getPath(e);
		return getRaw(path);
	}

	public byte[] getRaw(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		return getRaw(path);
	}

	public byte[] getRaw(String path) {
		var v = workspace.get(path);
		if (v == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		return v;
	}

	public ObjectId getRoot() {
		return get("");
	}

	public ObjectId get(ModelType type) {
		var path = getPath(type);
		return get(path);
	}

	public ObjectId get(RootEntity e) {
		var path = getPath(e);
		return get(path);
	}

	public ObjectId get(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		return get(path);
	}

	public ObjectId get(String path) {
		var id = workspace.get(path);
		if (id == null)
			return ObjectId.zeroId();
		return ObjectId.fromRaw(id);
	}

	public ObjectId getHead(String path) {
		var id = head.get(path);
		if (id == null)
			return ObjectId.zeroId();
		return ObjectId.fromRaw(id);
	}

	public void putRoot(ObjectId id) {
		put("", id);
	}

	public void put(ModelType type, ObjectId id) {
		var path = getPath(type);
		put(path, id);
	}

	public void put(RootEntity e, ObjectId id) {
		var path = getPath(e);
		put(path, id);
	}

	public void put(PathBuilder categoryPath, RootDescriptor d, ObjectId id) {
		var path = getPath(categoryPath, d);
		put(path, id);
	}

	public void put(String path, ObjectId id) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		var bytes = GitUtil.getBytes(id);
		workspace.put(path, bytes);
		head.put(path, bytes);
	}

	public void removeRoot() {
		remove("");
	}

	public void remove(ModelType type) {
		var path = getPath(type);
		remove(path);
	}

	public void remove(RootEntity e) {
		var path = getPath(e);
		remove(path);
	}

	public void remove(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		remove(path);
	}

	public void remove(String path) {
		var split = path.split("/");
		for (var i = 0; i < split.length; i++) {
			var k = "";
			for (var j = 0; j <= i; j++) {
				k += split[j];
				if (j < i) {
					k += "/";
				}
			}
			workspace.remove(k);
		}
		workspace.remove("");
		head.remove(path);
	}

	public void clear() {
		workspace.clear();
	}

	public String getPath(ModelType type) {
		return getPath(type, null, null);
	}

	public String getPath(RootEntity e) {
		var path = String.join("/", Categories.path(e.category));
		if (e instanceof Category)
			return getPath(((Category) e).modelType, path, e.name);
		return getPath(ModelType.forModelClass(e.getClass()), path, e.refId + GitUtil.DATASET_SUFFIX);
	}

	public String getPath(PathBuilder categoryPath, RootDescriptor d) {
		var path = categoryPath.pathOf(d.category);
		if (d.type == ModelType.CATEGORY)
			return getPath(((CategoryDescriptor) d).categoryType, path, d.name);
		return getPath(d.type, path, d.refId + GitUtil.DATASET_SUFFIX);
	}

	private String getPath(ModelType type, String path, String name) {
		var fullPath = type.name();
		if (path != null && !path.isBlank()) {
			if (!path.startsWith("/")) {
				fullPath += "/";
			}
			fullPath += path;
		}
		if (name != null && !name.isBlank()) {
			fullPath += "/" + name;
		}
		return fullPath;
	}

}
