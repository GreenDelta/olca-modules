package org.openlca.git.actions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.database.DataPackage;
import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.ConflictResolver.ConflictResolution;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionInfo;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ConflictResolver.GitContext;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ModelRefMap;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.util.Strings;
import org.openlca.util.TypedRefIdMap;
import org.openlca.util.TypedRefIdSet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class GitStoreReader implements JsonStoreReader {

	private static final Gson gson = new Gson();
	private final OlcaRepository repo;
	private final Commit localCommit;
	private final Categories categories;
	private final ModelRefMap<Reference> changes;
	private final byte[] repoInfo;
	private ConflictResolver conflictResolver;
	DataPackage dataPackage;
	final TypedRefIdSet tag = new TypedRefIdSet();
	final MergedDataImpl mergedData;

	GitStoreReader(OlcaRepository repo, Commit localCommit, Commit remoteCommit, List<Reference> changes) {
		this.repo = repo;
		this.categories = Categories.of(repo, remoteCommit.id);
		this.localCommit = localCommit;
		this.changes = new ModelRefMap<Reference>();
		this.repoInfo = repo.datasets.getRepositoryInfo(remoteCommit);
		changes.forEach(ref -> this.changes.put(ref, ref));
		this.mergedData = new MergedDataImpl(repo, localCommit);
	}

	GitStoreReader resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		return this;
	}

	GitStoreReader into(DataPackage dataPackage) {
		this.dataPackage = dataPackage;
		return this;
	}

	@Override
	public byte[] getBytes(String path) {
		if (RepositoryInfo.FILE_NAME.equals(path))
			return repoInfo;
		var binDir = GitUtil.findBinDir(path);
		if (binDir == null && GitUtil.isDatasetPath(path))
			return categories.getForPath(path);
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		if (binDir != null) {
			var refId = GitUtil.getRefId(binDir);
			var filepath = path.substring(binDir.length() + 1);
			var ref = changes.get(type, refId);
			return repo.datasets.getBinary(ref, filepath);
		}
		var refId = GitUtil.getRefId(path);
		var ref = changes.get(type, refId);
		return repo.datasets.getBytes(ref);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return categories.getForRefId(refId);
		if (!hasChanged(type, refId))
			return null;
		var ref = changes.get(type, refId);
		if (ref == null)
			return null;
		var peeked = conflictResolver.peekConflictResolution(ref);
		if (peeked != null && peeked.type == ConflictResolutionType.IS_EQUAL)
			return null;
		var remote = parse(repo.datasets.get(ref));
		if (!conflictResolver.isConflict(ref))
			return remote;
		var resolution = conflictResolver.resolveConflict(ref, remote);
		return resolveConflict(ref, resolution, remote);
	}

	private JsonObject resolveConflict(Reference ref, ConflictResolution resolution, JsonObject json) {
		if (resolution == null)
			throw new ConflictException(ref);
		if (resolution.context == GitContext.LOCAL) {
			resolveLocalConflict(ref, resolution);
			var workspaceResolution = conflictResolver.peekConflictResolutionWithWorkspace(ref);
			if (workspaceResolution != null && workspaceResolution.type != ConflictResolutionType.IS_EQUAL) {
				json = getLocalJson(ref, resolution, json);
				resolution = conflictResolver.resolveConflictWithWorkspace(ref, json);
				return resolveConflict(ref, resolution, json);
			}
		}
		if (resolution.type == ConflictResolutionType.KEEP)
			return null;
		if (resolution.type == ConflictResolutionType.IS_EQUAL && dataPackage != null) {
			tag.add(ref);
			return null;
		}
		if (resolution.type == ConflictResolutionType.OVERWRITE)
			return json;
		resolution.data.remove("dataPackage");
		return resolution.data;
	}

	private void resolveLocalConflict(Reference ref, ConflictResolution resolution) {
		switch (resolution.type) {
			case MERGE:
				resolveMerge(ref, resolution);
				break;
			case OVERWRITE:
				resolveOverwrite(ref, resolution);
				break;
			case KEEP:
				resolveKeep(ref, resolution);
				break;
			case IS_EQUAL:
				break;
		}
	}

	private JsonObject getLocalJson(Reference ref, ConflictResolution resolution, JsonObject remoteJson) {
		return switch (resolution.type) {
			case MERGE -> resolution.data;
			case OVERWRITE -> remoteJson;
			case KEEP -> parse(repo.datasets.get(repo.references.get(ref.type, ref.refId, localCommit.id)));
			case IS_EQUAL -> null; // not relevant
		};
	}

	private void resolveKeep(Reference ref, ConflictResolutionInfo resolution) {
		if (localCommit == null || resolution.context == GitContext.WORKSPACE)
			return;
		var localRef = repo.references.get(ref.type, ref.refId, localCommit.id);
		if (localRef == null) {
			mergedData.keep(Diff.deleted(ref));
		} else if (!ref.path.equals(localRef.path)) {
			mergedData.keep(Diff.moved(ref, localRef));
		} else {
			mergedData.keep(Diff.modified(ref, localRef));
		}
	}

	private void resolveOverwrite(Reference ref, ConflictResolution resolution) {
		if (resolution.context == GitContext.WORKSPACE)
			return;
		// commit writer will use remote commit version when no conflict
		// resolution change is provided, only in case of a move, the
		// deletion needs to be applied to the repo otherwise the dataset will
		// appear in both categories
		var localRef = repo.references.get(ref.type, ref.refId, localCommit.id);
		if (localRef != null && !ref.path.equals(localRef.path)) {
			mergedData.delete(Diff.deleted(localRef));
		}
	}

	private void resolveMerge(Reference ref, ConflictResolution resolution) {
		if (localCommit == null || resolution.context == GitContext.WORKSPACE)
			return;
		var localRef = repo.references.get(ref.type, ref.refId, localCommit.id);
		var category = Json.getString(resolution.data, "category");
		var mergedPath = GitUtil.toDatasetPath(localRef.type, category, localRef.refId);
		var mergedRef = new Reference(mergedPath);
		if (!mergedPath.equals(localRef.path)) {
			mergedData.merged(Diff.moved(localRef, mergedRef), resolution.data);
		} else if (!mergedPath.equals(ref.path)) {
			mergedData.merged(Diff.moved(ref, mergedRef), resolution.data);
		} else {
			mergedData.merged(Diff.modified(localRef, mergedRef), resolution.data);
		}
	}

	private boolean hasChanged(ModelType type, String refId) {
		return changes.contains(type, refId);
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return Collections.emptyList();
		var ref = changes.get(type, refId);
		if (ref == null)
			return Collections.emptyList();
		return repo.references.getBinaries(ref).stream()
				.map(binary -> ref.getBinariesPath() + "/" + binary)
				.toList();
	}

	@Override
	public List<String> getFiles(String dir) {
		// TODO relevant for upgrades
		return new ArrayList<>();
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		return getChanges(type).stream()
				.map(r -> r.refId)
				.toList();
	}

	List<? extends ModelRef> getChanges(ModelType type) {
		if (type == ModelType.CATEGORY)
			return new ArrayList<>();
		return changes.get(type).stream()
				.map(this::replaceEqualOrKeptWithNull)
				.collect(Collectors.toList());
	}

	int size() {
		return changes.size();
	}

	private Reference replaceEqualOrKeptWithNull(Reference ref) {
		// performance improvement: JsonImport will load model from
		// database. If ref will not be imported and conflict
		// resolver can determine resolution without json data we
		// can skip that. Returning null values to still display progress
		// in ImportData
		var resolution = conflictResolver.peekConflictResolution(ref);
		if (resolution == null)
			return ref;
		if (resolution.context == GitContext.LOCAL) {
			if (resolution.type == ConflictResolutionType.KEEP) {
				resolveKeep(ref, resolution);
			}
			var workspaceResolution = conflictResolver.peekConflictResolutionWithWorkspace(ref);
			if (workspaceResolution != null && workspaceResolution.type != ConflictResolutionType.IS_EQUAL) {
				resolution = workspaceResolution;
			}
		}
		if (resolution.type == ConflictResolutionType.KEEP)
			return null;
		if (resolution.type == ConflictResolutionType.IS_EQUAL && dataPackage != null) {
			tag.add(ref);
			return null;
		}
		return ref;
	}

	private JsonObject parse(String data) {
		if (Strings.nullOrEmpty(data))
			return null;
		var dataset = gson.fromJson(data, JsonObject.class);
		if (dataPackage != null) {
			dataset.addProperty("dataPackage", dataPackage.name());
		}
		return dataset;
	}

	static class MergedDataImpl implements MergedData {

		private final OlcaRepository repo;
		private final Commit localCommit;
		private final List<Diff> diffs = new ArrayList<>();
		private final TypedRefIdSet keep = new TypedRefIdSet();
		private final TypedRefIdMap<JsonObject> merged = new TypedRefIdMap<>();

		MergedDataImpl(OlcaRepository repo, Commit localCommit) {
			this.repo = repo;
			this.localCommit = localCommit;
		}

		void delete(Diff diff) {
			diffs.add(diff);
		}

		void keep(Diff diff) {
			diffs.add(diff);
			keep.add(diff);
		}

		void merged(Diff diff, JsonObject mergedData) {
			diffs.add(diff);
			merged.put(diff, mergedData);
		}

		@Override
		public byte[] get(Diff diff) {
			if (merged.contains(diff))
				try {
					return gson.toJson(merged.get(diff)).getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					return null;
				}
			if (keep.contains(diff)) {
				var ref = repo.references.get(diff.type, diff.refId, localCommit.id);
				return repo.datasets.getBytes(ref);
			}
			return null;
		}

		@Override
		public List<Diff> getDiffs() {
			return diffs;
		}

	}

}
