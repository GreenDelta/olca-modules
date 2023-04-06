package org.openlca.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

public class GitIndex {

	private final File file;
	private Map<String, GitIndexEntry> entries = new HashMap<>();
	private Map<String, Set<String>> subPaths = new HashMap<>();

	private GitIndex(File file) {
		this.file = file;
	}

	@SuppressWarnings("unchecked")
	public static GitIndex fromFile(File file) throws IOException {
		var index = new GitIndex(file);
		if (file == null || !file.exists())
			return index;
		try (var fis = new FileInputStream(file);
				var ois = new ObjectInputStream(fis)) {
			index.entries = (HashMap<String, GitIndexEntry>) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		for (var key : index.entries.keySet()) {
			index.addSubPath(key);
		}
		return index;
	}

	private void addSubPath(String path) {
		if (path.isEmpty())
			return;
		var parent = getParent(path);
		subPaths.computeIfAbsent(parent, k -> new HashSet<>()).add(path);
	}

	private void removeSubPath(String path) {
		if (path.isEmpty())
			return;
		var parent = getParent(path);
		var set = subPaths.get(parent);
		if (set == null)
			return;
		set.remove(path);
		if (set.isEmpty()) {
			subPaths.remove(parent);
		}
	}

	private String getParent(String path) {
		var lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1)
			return "";
		return path.substring(0, lastSlash);
	}

	public static GitIndex inMemory() {
		return new GitIndex(null);
	}

	public void save() throws IOException {
		if (file == null)
			return;
		if (!file.getParentFile().exists()) {
			Files.createDirectories(file.getParentFile().toPath());
		}
		try (var fos = new FileOutputStream(file);
				var oos = new ObjectOutputStream(fos)) {
			oos.writeObject(entries);
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
		if (path == null)
			return false;
		var entry = entries.get(path);
		return entry != null && entry.objectId != null;
	}

	public GitIndexEntry getRoot() {
		return get("");
	}

	public GitIndexEntry get(ModelType type) {
		var path = getPath(type);
		return get(path);
	}

	public GitIndexEntry get(RootEntity e) {
		var path = getPath(e);
		return get(path);
	}

	public GitIndexEntry get(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		return get(path);
	}

	public GitIndexEntry get(String path) {
		if (path == null)
			return GitIndexEntry.NULL;
		var entry = entries.get(path);
		if (entry != null)
			return entry;
		return GitIndexEntry.NULL;
	}

	public Set<String> getSubPaths(String path) {
		if (path == null)
			return new HashSet<>();
		return subPaths.getOrDefault(path, new HashSet<>());
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
		put(path, e.version, e.lastChange, id);
	}

	public void put(PathBuilder categoryPath, RootDescriptor d, ObjectId id) {
		var path = getPath(categoryPath, d);
		put(path, d.version, d.lastChange, id);
	}

	public void put(String path, ObjectId id) {
		put(path, -1, -1, id);
	}

	public void put(String path, long version, long lastChange, ObjectId id) {
		if (path == null)
			return;
		entries.put(path, new GitIndexEntry(path, version, lastChange, id));
		addSubPath(path);
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
		if (path == null)
			return;
		entries.remove(path);
		removeSubPath(path);
	}

	public void invalidate() {
		invalidate("");
	}

	public void invalidate(ModelType type) {
		var path = getPath(type);
		invalidate(path);
	}

	public void invalidate(RootEntity e) {
		var path = getPath(e);
		invalidate(path);
	}

	public void invalidate(PathBuilder categoryPath, RootDescriptor d) {
		var path = getPath(categoryPath, d);
		invalidate(path);
	}

	public void invalidate(String path) {
		if (path == null)
			return;
		var split = path.split("/");
		for (var i = 0; i < split.length; i++) {
			var k = "";
			for (var j = 0; j <= i; j++) {
				k += split[j];
				if (j < i) {
					k += "/";
				}
			}
			invalidate(entries.get(k));
		}
		invalidate(entries.get(""));
	}

	private void invalidate(GitIndexEntry entry) {
		if (entry == null)
			return;
		entry.objectId = null;
	}

	public void clear() {
		entries.clear();
		subPaths.clear();
	}

	public String getPath(ModelType type) {
		return getPath(type, null, null);
	}

	public String getPath(RootEntity e) {
		var path = String.join("/", Categories.path(e.category));
		if (e instanceof Category)
			return getPath(((Category) e).modelType, path, e.name);
		return getPath(ModelType.of(e), path, e.refId + GitUtil.DATASET_SUFFIX);
	}

	public String getPath(PathBuilder categoryPath, RootDescriptor d) {
		var path = categoryPath.pathOf(d.category);
		if (d.type == ModelType.CATEGORY)
			return getPath(((CategoryDescriptor) d).categoryType, path, d.name);
		return getPath(d.type, path, d.refId + GitUtil.DATASET_SUFFIX);
	}

	public String getPath(ModelType type, String path, String name) {
		if (type == null)
			return null;
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

	public static class GitIndexEntry implements Serializable {

		public static final GitIndexEntry NULL = new GitIndexEntry(null, -1, -1, null);
		private static final long serialVersionUID = 2035250054845500724L;
		private final long version;
		private final long lastChange;
		private byte[] objectId;

		private GitIndexEntry(String path, long version, long lastChange, ObjectId objectId) {
			this.version = version;
			this.lastChange = lastChange;
			this.objectId = objectId != null && !objectId.equals(ObjectId.zeroId())
					? GitUtil.getBytes(objectId)
					: null;
		}

		public long version() {
			return version;
		}

		public long lastChange() {
			return lastChange;
		}

		public ObjectId objectId() {
			if (objectId == null)
				return ObjectId.zeroId();
			return ObjectId.fromRaw(objectId);
		}

		public byte[] rawObjectId() {
			if (objectId == null)
				return GitUtil.getBytes(ObjectId.zeroId());
			return objectId;
		}

	}

}
