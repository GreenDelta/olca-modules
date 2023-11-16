package org.openlca.git.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.model.Entry;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.jsonld.Json;

public class HeadIndex {

	private final OlcaRepository repo;
	private Map<String, Entry> map;
	private Map<String, Set<String>> subPaths = new HashMap<>();
	private Map<String, MetaInfo> metaInfo = new HashMap<>();

	private HeadIndex(OlcaRepository repo) {
		this.repo = repo;
		reload();
	}

	public void reload() {
		subPaths.clear();
		metaInfo.clear();
		map = repo.entries.find().recursive().asMap();
		map.values().forEach(entry -> {
			if (entry.typeOfEntry == EntryType.DATASET) {
				metaInfo.put(entry.path, new MetaInfo(repo.datasets.getVersionAndLastChange(entry)));
			}
			var path = entry.path;
			var parent = path.contains("/")
					? path.substring(0, path.lastIndexOf("/"))
					: "";
			subPaths.computeIfAbsent(parent, k -> new HashSet<>()).add(path);
		});
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

	public boolean isSameVersion(String path, RootDescriptor d) {
		var info = metaInfo.get(path);
		if (info == null)
			return false;
		return info.matches(d);
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
