package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ImportResults.ImportState;
import org.openlca.git.find.Datasets;
import org.openlca.git.find.Entries;
import org.openlca.git.find.References;
import org.openlca.git.model.Commit;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.TypedRefIdMap;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.util.Strings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

class GitStoreReader implements JsonStoreReader {

	private static final Gson gson = new Gson();
	private final References references;
	private final Datasets datasets;
	private final Commit previousCommit;
	private final Commit commit;
	private final Categories categories;
	private final TypedRefIdMap<Reference> changes;
	private final ConflictResolver conflictResolver;
	private final ImportResults results = new ImportResults();
	private final byte[] repoInfo;

	GitStoreReader(Repository repo, Commit remoteCommit, List<Reference> remoteChanges) {
		this(repo, null, remoteCommit, remoteChanges, null);
	}

	GitStoreReader(Repository repo, Commit previousCommit, Commit commit, List<Reference> changes,
			ConflictResolver conflictResolver) {
		this.categories = Categories.of(Entries.of(repo), commit.id);
		this.references = References.of(repo);
		this.datasets = Datasets.of(repo);
		this.previousCommit = previousCommit;
		this.commit = commit;
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		this.changes = new TypedRefIdMap<Reference>();
		changes.forEach(ref -> {
			if (ref.isCategory) {
				this.changes.put(ref.type, ref.path, ref);
			} else {
				this.changes.put(ref, ref);
			}
		});
		this.repoInfo = datasets.getRepositoryInfo(commit);

	}

	@Override
	public byte[] getBytes(String path) {
		if (RepositoryInfo.FILE_NAME.equals(path))
			return repoInfo;
		var binDir = GitUtil.findBinDir(path);
		if (binDir == null && !path.endsWith(GitUtil.DATASET_SUFFIX))
			return categories.getForPath(path);
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		var refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
		var ref = binDir == null
				? changes.get(type, refId)
				: references.get(type, refId, commit.id);
		return datasets.getBytes(ref);
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
		if (conflictResolver.peekConflictResolution(ref) == ConflictResolutionType.IS_EQUAL) {
			results.add(ref, ImportState.UPDATED);
			return null;
		}
		var data = datasets.get(ref);
		var remote = parse(data);
		if (!conflictResolver.isConflict(ref)) {
			results.add(ref, ImportState.UPDATED);
			return remote;
		}
		var resolution = conflictResolver.resolveConflict(ref, remote);
		if (resolution.type == ConflictResolutionType.IS_EQUAL) {
			results.add(ref, ImportState.UPDATED);
			return null;
		}
		if (resolution.type == ConflictResolutionType.OVERWRITE) {
			results.add(ref, ImportState.UPDATED);
			return remote;
		}
		if (resolution.type == ConflictResolutionType.KEEP && previousCommit != null) {
			if (references.get(type, refId, previousCommit.id) == null) {
				results.add(ref, ImportState.KEPT_DELETED);
			}
			return null;
		}
		results.add(ref, ImportState.MERGED);
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
		return references.getBinaries(ref).stream()
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
		return changes.get(type).stream().map(ref -> {
			// performance improvement: JsonImport will load model from
			// database. If ref will not be imported and conflict resolver can
			// determine resolution without json data we can skip that.
			// ObjectIds need still to be updated so refs need to be added to
			// results. Returning null values to count worked refs in
			// ImportHelper
			var resolution = conflictResolver.peekConflictResolution(ref);
			if (resolution == ConflictResolutionType.IS_EQUAL) {
				results.add(ref, ImportState.UPDATED);
				return null;
			}
			if (resolution == ConflictResolutionType.KEEP && previousCommit != null) {
				if (references.get(type, ref.refId, previousCommit.id) == null) {
					results.add(ref, ImportState.KEPT_DELETED);
				} else {
					results.add(ref, ImportState.KEPT);
				}
				return null;
			}
			return ref;
		}).collect(Collectors.toList());
	}

	private JsonObject parse(String data) {
		if (Strings.nullOrEmpty(data))
			return null;
		return gson.fromJson(data, JsonObject.class);
	}

	ImportResults getResults() {
		return results;
	}

}
