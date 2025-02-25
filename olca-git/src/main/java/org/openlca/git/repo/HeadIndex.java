package org.openlca.git.repo;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.TypedRefId;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.model.Reference;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.openlca.util.TypedRefIdMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class HeadIndex {

	private static final int VERSION = 1;
	private static final String FILE_NAME = "head.index";
	private final OlcaRepository repo;
	private final Map<String, Set<String>> subPaths = new HashMap<>();
	private final Map<String, MetaInfo> metaInfo = new HashMap<>();
	private final TypedRefIdMap<String> pathsByRef = new TypedRefIdMap<>();
	private Map<String, Reference> map = new HashMap<>();

	private HeadIndex(OlcaRepository repo) {
		this.repo = repo;
		load();
		fillUp();
	}

	static HeadIndex of(OlcaRepository repo) {
		return new HeadIndex(repo);
	}

	public ObjectId getObjectId(String path) {
		var entry = map.get(path);
		if (entry == null)
			return ObjectId.zeroId();
		return entry.objectId;
	}

	public boolean contains(String path) {
		return map.containsKey(path);
	}

	public Set<String> getSubPaths(String path) {
		return subPaths.getOrDefault(path, new HashSet<>());
	}

	public String getPath(TypedRefId typedRefId) {
		return pathsByRef.get(typedRefId);
	}

	public boolean isSameVersion(String path, RootDescriptor d) {
		var info = metaInfo.get(path);
		if (info == null)
			return false;
		return info.matches(d);
	}

	private void load() {
		metaInfo.clear();
		var file = new File(repo.dir, FILE_NAME);
		if (!file.exists())
			return;
		var o = Json.readObject(file).orElse(null);
		if (o == null) {
			file.delete();
			return;
		}
		var indexVersion = Json.getInt(o, "version").orElse(0);
		if (indexVersion != VERSION) {
			file.delete();
			return;
		}
		var array = Json.getArray(o, "metaInfo");
		if (array.isEmpty())
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var path = Json.getString(obj, "p");
			if (Strings.nullOrEmpty(path))
				continue;
			var version = Json.getLong(obj, "v", 0);
			var lastChange = Json.getLong(obj, "l", 0);
			metaInfo.put(path, new MetaInfo(version, lastChange));
		}
	}

	public void reload() {
		metaInfo.clear();
		fillUp();
	}

	private void fillUp() {
		try {
			subPaths.clear();
			pathsByRef.clear();
			map = repo.references.find().includeCategories().asMap();
			var changed = false;
			for (var entry : map.values()) {
				var path = entry.path;
				var parent = path.contains("/")
						? path.substring(0, path.lastIndexOf("/"))
						: "";
				subPaths.computeIfAbsent(parent, k -> new HashSet<>()).add(path);
				if (!entry.isDataset)
					continue;
				pathsByRef.put(entry, entry.path);
				if (metaInfo.containsKey(entry.path))
					continue;
				metaInfo.put(entry.path, new MetaInfo(repo.datasets.getVersionAndLastChange(entry)));
				changed = true;
			}
			if (!changed)
				return;
			store();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void store() {
		var o = new JsonObject();
		o.addProperty("version", VERSION);
		var array = new JsonArray();
		for (var path : metaInfo.keySet()) {
			var obj = new JsonObject();
			obj.addProperty("p", path);
			var value = metaInfo.get(path);
			obj.addProperty("v", value.version);
			obj.addProperty("l", value.lastChange);
			array.add(obj);
		}
		o.add("metaInfo", array);
		Json.write(o, new File(repo.dir, FILE_NAME));
	}

	private static class MetaInfo {

		private final long version;
		private final long lastChange;

		private MetaInfo(Map<String, Object> info) {
			this(getVersion(info), getLastChange(info));
		}

		private MetaInfo(long version, long lastChange) {
			this.version = version;
			this.lastChange = lastChange;
		}

		private boolean matches(RootDescriptor d) {
			return d.version == version
					&& d.lastChange == lastChange;
		}

		private static long getVersion(Map<String, Object> meta) {
			var version = meta.get("version");
			if (version == null)
				return 0;
			return Version.fromString(version.toString()).getValue();
		}

		private static long getLastChange(Map<String, Object> meta) {
			var lastChange = meta.get("lastChange");
			if (lastChange == null)
				return 0;
			var date = Json.parseDate(lastChange.toString());
			return date != null
					? date.getTime()
					: 0;
		}
	}

}
