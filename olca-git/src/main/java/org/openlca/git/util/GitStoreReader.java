package org.openlca.git.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
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
import org.openlca.jsonld.SchemaVersion;
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
	private final List<Reference> remoteChanges;
	private final ConflictResolver conflictResolver;
	private final List<Reference> imported = new ArrayList<>();
	private final List<Reference> merged = new ArrayList<>();
	private final List<Reference> keepDeleted = new ArrayList<>();

	public GitStoreReader(FileRepository repo, Commit localCommit, Commit remoteCommit,
			ConflictResolver conflictResolver) {
		this.categories = Categories.of(Entries.of(repo), remoteCommit.id);
		this.references = References.of(repo);
		this.datasets = Datasets.of(repo);
		this.ids = Ids.of(repo);
		this.localCommit = localCommit;
		this.remoteCommit = remoteCommit;
		this.conflictResolver = conflictResolver;
		this.remoteChanges = references.find()
				.commit(remoteCommit.id)
				.changedSince(localCommit != null ? localCommit.id : null)
				.all();
	}

	public boolean contains(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return true;
		return references.get(type, refId, remoteCommit.id) != null;
	}

	@Override
	public byte[] getBytes(String path) {
		if (SchemaVersion.FILE_NAME.equals(path))
			return getSchema();
		var binDir = GitUtil.findBinDir(path);
		if (binDir == null && !path.endsWith(GitUtil.DATASET_SUFFIX))
			return categories.getForPath(path);
		if (binDir == null)
			return getDataset(path);
		return getBinary(binDir, path);
	}

	private byte[] getSchema() {
		var objectId = ids.get(SchemaVersion.FILE_NAME, remoteCommit.id);
		return datasets.getBytes(objectId);
	}

	private byte[] getDataset(String path) {
		var ref = getRef(path);
		if (ref == null)
			return null;
		return datasets.getBytes(ref.objectId);
	}

	private Reference getRef(String path) {
		var refs = references.find().path(path).commit(remoteCommit.id).all();
		if (refs.isEmpty())
			return null;
		if (refs.size() > 1)
			throw new IllegalArgumentException("Ambigious path, returned more then 1 reference");
		return refs.get(0);
	}

	private byte[] getBinary(String binDir, String path) {
		var i = binDir.lastIndexOf(GitUtil.BIN_DIR_SUFFIX);
		var refPath = binDir.substring(0, i) + GitUtil.DATASET_SUFFIX;
		var filepath = path.substring(i + GitUtil.BIN_DIR_SUFFIX.length() + 1);
		var ref = getRef(refPath);
		return datasets.getBinary(ref, filepath);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return categories.getForRefId(refId);
		var ref = references.get(type, refId, remoteCommit.id);
		if (ref == null || !hasChanged(ref))
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
		if (resolution.type == ConflictResolutionType.KEEP_LOCAL) {
			if (references.get(type, refId, localCommit.id) == null) {
				keepDeleted.add(ref);
			}
			return null;
		}
		merged.add(ref);
		return resolution.data;
	}

	private boolean hasChanged(ModelRef ref) {
		return remoteChanges.stream()
				.anyMatch(r -> r.type == ref.type && r.refId.equals(ref.refId));
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		if (type == ModelType.CATEGORY)
			return Collections.emptyList();
		var ref = references.get(type, refId, remoteCommit.id);
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

	public List<Reference> getChanges(ModelType type) {
		if (type == ModelType.CATEGORY)
			return Collections.emptyList();
		return remoteChanges.stream()
				.filter(ref -> ref.type == type)
				.toList();
	}

	private JsonObject parse(String data) {
		if (Strings.nullOrEmpty(data))
			return null;
		return gson.fromJson(data, JsonObject.class);
	}

	public List<Reference> getImported() {
		return imported;
	}

	public List<Reference> getMerged() {
		return merged;
	}

	public List<Reference> getKeepDeleted() {
		return keepDeleted;
	}

}
