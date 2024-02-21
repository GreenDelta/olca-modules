package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.TypedRefIdMap;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.util.Strings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class GitStoreReader implements JsonStoreReader {

	private static final Gson gson = new Gson();
	private final OlcaRepository repo;
	private final Commit previousCommit;
	private final Commit commit;
	private final Categories categories;
	private final TypedRefIdMap<Reference> changes;
	private final ConflictResolver conflictResolver;
	private final byte[] repoInfo;
	final Set<Change> resolvedConflicts = new HashSet<>();

	GitStoreReader(OlcaRepository repo, Commit remoteCommit, List<Reference> remoteChanges) {
		this(repo, null, remoteCommit, remoteChanges, null);
	}

	GitStoreReader(OlcaRepository repo, Commit previousCommit, Commit commit, List<Reference> changes,
			ConflictResolver conflictResolver) {
		this.repo = repo;
		this.categories = Categories.of(repo, commit.id);
		this.previousCommit = previousCommit;
		this.commit = commit;
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		this.changes = new TypedRefIdMap<Reference>();
		this.repoInfo = repo.datasets.getRepositoryInfo(commit);
		changes.forEach(ref -> {
			if (ref.isCategory) {
				this.changes.put(ref.type, ref.path, ref);
			} else {
				this.changes.put(ref, ref);
			}
		});
	}

	@Override
	public byte[] getBytes(String path) {
		if (RepositoryInfo.FILE_NAME.equals(path))
			return repoInfo;
		var binDir = GitUtil.findBinDir(path);
		if (binDir == null && !path.endsWith(GitUtil.DATASET_SUFFIX))
			return categories.getForPath(path);
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		if (binDir != null) {
			var refId = binDir.substring(binDir.lastIndexOf("/") + 1, binDir.lastIndexOf(GitUtil.BIN_DIR_SUFFIX));
			var ref = repo.references.get(type, refId, commit.id);
			return repo.datasets.getBytes(ref);
		}
		var refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
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
		if (conflictResolver.peekConflictResolution(ref) == ConflictResolutionType.IS_EQUAL)
			return null;
		var data = repo.datasets.get(ref);
		var remote = parse(data);
		if (!conflictResolver.isConflict(ref))
			return remote;
		var resolution = conflictResolver.resolveConflict(ref, remote);
		if (resolution.type == ConflictResolutionType.IS_EQUAL)
			return null;
		if (resolution.type == ConflictResolutionType.OVERWRITE)
			return remote;
		if (resolution.type == ConflictResolutionType.KEEP && previousCommit != null) {
			resolveKeep(ref);
			return null;
		}
		resolvedConflicts.add(Change.modify(ref));
		return resolution.data;
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
		if (resolution == ConflictResolutionType.IS_EQUAL)
			return null;
		if (resolution == ConflictResolutionType.KEEP && previousCommit != null) {
			resolveKeep(ref);
			return null;
		}
		return ref;
	}
	
	private void resolveKeep(Reference ref) {
		var previousRef = repo.references.get(ref.type, ref.refId, previousCommit.id);
		if (previousRef == null) {
			resolvedConflicts.add(Change.delete(ref));
		} else if (!ref.path.equals(previousRef.path)) {
			resolvedConflicts.addAll(Change.move(ref, previousRef));
		} else {
			resolvedConflicts.add(Change.modify(previousRef));
		}
	}

	private JsonObject parse(String data) {
		if (Strings.nullOrEmpty(data))
			return null;
		return gson.fromJson(data, JsonObject.class);
	}

}
