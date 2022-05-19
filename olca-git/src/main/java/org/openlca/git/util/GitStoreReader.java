package org.openlca.git.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.ModelType;
import org.openlca.git.actions.ConflictResolver;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.find.Datasets;
import org.openlca.git.find.Entries;
import org.openlca.git.find.Ids;
import org.openlca.git.find.References;
import org.openlca.git.model.Commit;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.PackageInfo;
import org.openlca.util.Strings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GitStoreReader implements JsonStoreReader {

	private static final Gson gson = new Gson();
	private final References references;
	private final Datasets datasets;
	private final Ids ids;
	private final Commit localCommit;
	private final Commit remoteCommit;
	private final Categories categories;
	private final TypeRefIdMap<Reference> remoteChanges;
	private final ConflictResolver conflictResolver;
	private final List<Reference> imported = new ArrayList<>();
	private final List<ModelRef> merged = new ArrayList<>();
	private final List<ModelRef> keepDeleted = new ArrayList<>();

	public GitStoreReader(FileRepository repo, Commit remoteCommit, List<Reference> remoteChanges) {
		this(repo, null, remoteCommit, remoteChanges, null);
	}

	public GitStoreReader(FileRepository repo, Commit localCommit, Commit remoteCommit, List<Reference> remoteChanges,
			ConflictResolver conflictResolver) {
		this.categories = Categories.of(Entries.of(repo), remoteCommit.id);
		this.references = References.of(repo);
		this.datasets = Datasets.of(repo);
		this.ids = Ids.of(repo);
		this.localCommit = localCommit;
		this.remoteCommit = remoteCommit;
		this.conflictResolver = conflictResolver;
		this.remoteChanges = new TypeRefIdMap<>();
		remoteChanges.forEach(d -> GitStoreReader.this.remoteChanges.put(d.type, d.refId, d));
	}

	public boolean contains(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return true;
		return remoteChanges.contains(type, refId);
	}

	@Override
	public byte[] getBytes(String path) {
		if (PackageInfo.FILE_NAME.equals(path))
			return getPackInfo();
		var binDir = GitUtil.findBinDir(path);
		if (binDir == null && !path.endsWith(GitUtil.DATASET_SUFFIX))
			return categories.getForPath(path);
		if (binDir == null)
			return getDataset(path);
		var objectId = ids.get(path, remoteCommit.id);
		if (ObjectId.zeroId().equals(objectId))
			return null;
		return datasets.getBytes(objectId);
	}

	private byte[] getPackInfo() {
		var objectId = ids.get(PackageInfo.FILE_NAME, remoteCommit.id);
		return datasets.getBytes(objectId);
	}

	private byte[] getDataset(String path) {
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		var refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".json"));
		var ref = remoteChanges.get(type, refId);
		if (ObjectId.zeroId().equals(ref.objectId))
			return null;
		return datasets.getBytes(ref.objectId);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return categories.getForRefId(refId);
		if (!hasChanged(type, refId))
			return null;
		var ref = remoteChanges.get(type, refId);
		if (ref == null)
			return null;
		var data = datasets.get(ref.objectId);
		var remote = parse(data);
		if (conflictResolver == null || !conflictResolver.isConflict(ref)) {
			imported.add(ref);
			return remote;
		}
		var resolution = conflictResolver.resolveConflict(ref, remote);
		if (resolution.type == ConflictResolutionType.OVERWRITE_LOCAL) {
			imported.add(ref);
			return remote;
		}
		if (resolution.type == ConflictResolutionType.KEEP_LOCAL && localCommit != null) {
			if (references.get(type, refId, localCommit.id) == null) {
				keepDeleted.add(new ModelRef(ref));
			}
			return null;
		}
		merged.add(ref);
		return resolution.data;
	}

	private boolean hasChanged(ModelType type, String refId) {
		return remoteChanges.contains(type, refId);
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return Collections.emptyList();
		var ref = remoteChanges.get(type, refId);
		if (ref == null)
			return Collections.emptyList();
		return references.getBinaries(ref).stream()
				.map(binary -> ref.getBinariesPath() + "/" + binary)
				.toList();
	}

	@Override
	public List<String> getFiles(String dir) {
		throw new UnsupportedOperationException("Not supported by this implementation");
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		return getChanges(type).stream()
				.map(r -> r.refId)
				.toList();
	}

	public List<? extends ModelRef> getChanges(ModelType type) {
		if (type == ModelType.CATEGORY)
			return Collections.emptyList();
		return remoteChanges.get(type);
	}

	private JsonObject parse(String data) {
		if (Strings.nullOrEmpty(data))
			return null;
		return gson.fromJson(data, JsonObject.class);
	}

	public List<Reference> getImported() {
		return imported;
	}

	public List<ModelRef> getMerged() {
		return merged;
	}

	public List<ModelRef> getKeepDeleted() {
		return keepDeleted;
	}

}
