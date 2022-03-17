package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.GitConfig;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.find.Commits;
import org.openlca.git.find.Entries;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Constants;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.util.GitStoreReader;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

public class GitMerge {

	private static final ModelType[] typeOrder = new ModelType[] {
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.PARAMETER,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.CURRENCY,
			ModelType.FLOW,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD
	};
	private final FileRepository git;
	private final Commits commits;
	private final Entries entries;
	private IDatabase database;
	private ObjectIdStore workspaceIds;
	private ConflictResolver conflictResolver;

	private GitMerge(FileRepository git) {
		this.git = git;
		this.commits = Commits.of(git);
		this.entries = Entries.of(git);
	}

	public static GitMerge from(FileRepository git) {
		return new GitMerge(git);
	}

	public GitMerge into(IDatabase database) {
		this.database = database;
		return this;
	}

	public GitMerge update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	public GitMerge resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	public List<Reference> run() throws IOException {
		if (git == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		var localCommitId = commits.resolve(Constants.LOCAL_BRANCH);
		var remoteCommitId = commits.resolve(Constants.REMOTE_BRANCH);
		var behind = commits.find()
				.after(localCommitId)
				.until(remoteCommitId)
				.all();
		if (behind.isEmpty())
			return new ArrayList<>();
		var localChanges = getLocalChanges(localCommitId);
		var deleted = getDeletedInRemote(localCommitId, remoteCommitId);
		var imported = merge(localCommitId, remoteCommitId, localChanges, deleted);
		updateHeadRef(remoteCommitId);
		updateWorkspaceIds(remoteCommitId, imported, localChanges, deleted);
		return imported;
	}

	private List<Reference> getLocalChanges(String localCommitId) throws IOException {
		var commit = commits.get(localCommitId);
		var config = new GitConfig(database, workspaceIds, git, null);
		return DiffEntries.workspace(config, commit).stream()
				.map(d -> new Diff(d).left)
				.collect(Collectors.toList());
	}

	private List<Reference> getDeletedInRemote(String localCommitId, String remoteCommitId) throws IOException {
		var localCommit = commits.get(localCommitId);
		var remoteCommit = commits.get(remoteCommitId);
		return DiffEntries.between(git, localCommit, remoteCommit).stream()
				.filter(d -> d.getChangeType() == ChangeType.DELETE)
				.map(d -> new Diff(d).left)
				.collect(Collectors.toList());
	}

	private List<Reference> merge(String localCommitId, String remoteCommitId, List<Reference> localChanges,
			List<Reference> remoteDeletions) {
		var gitStore = new GitStoreReader(git, localCommitId, remoteCommitId);
		gitStore.setLocalChanges(localChanges);
		gitStore.setConflictResolver(conflictResolver);
		var imported = new ArrayList<Reference>();
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : typeOrder) {
			var changes = gitStore.getChanges(type);
			for (var change : changes) {
				jsonImport.run(type, change.refId);
				imported.add(change);
			}
		}
		for (var ref : new ArrayList<>(remoteDeletions)) {
			if (conflictResolver.isConflict(ref)) {
				var resolution = conflictResolver.resolveConflict(ref, null);
				if (resolution.type == ConflictResolutionType.KEEP_LOCAL) {
					remoteDeletions.remove(ref);
					continue;
				}
			}
			delete(Daos.root(database, ref.type), ref.refId);
		}
		return imported;
	}

	private <T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	private void updateWorkspaceIds(String remoteCommitId, List<Reference> imported, List<Reference> localChanges,
			List<Reference> deleted)
			throws IOException {
		if (workspaceIds == null)
			return;
		imported.forEach(ref -> workspaceIds.put(ref.fullPath, ref.objectId));
		deleted.forEach(ref -> workspaceIds.invalidate(ref.fullPath));
		updateCategoryIds(remoteCommitId, "");
		workspaceIds.putRoot(ObjectId.fromString(remoteCommitId));
		localChanges.forEach(ref -> workspaceIds.invalidate(ref.fullPath));
		workspaceIds.save();
	}

	private void updateCategoryIds(String remoteCommitId, String path) {
		entries.find().commit(remoteCommitId).path(path).all().forEach(entry -> {
			if (entry.typeOfEntry == EntryType.DATASET)
				return;
			workspaceIds.put(entry.fullPath, entry.objectId);
			updateCategoryIds(remoteCommitId, entry.fullPath);
		});
	}

	private void updateHeadRef(String remoteCommitId) throws IOException {
		var update = git.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommitId));
		update.update();
	}

}
