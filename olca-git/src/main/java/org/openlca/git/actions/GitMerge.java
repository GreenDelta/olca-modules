package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
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
import org.openlca.git.find.References;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Constants;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.util.GitStoreReader;
import org.openlca.git.util.History;
import org.openlca.git.writer.CommitWriter;
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
	private final History history;
	private final Commits commits;
	private final Entries entries;
	private final References references;
	private IDatabase database;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;
	private ConflictResolver conflictResolver;

	private GitMerge(FileRepository git) {
		this.git = git;
		this.history = History.of(git);
		this.commits = Commits.of(git);
		this.entries = Entries.of(git);
		this.references = References.of(git);
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

	public GitMerge as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitMerge resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	public boolean run() throws IOException {
		if (git == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		var behind = history.getBehind();
		if (behind.isEmpty())
			return false;
		var ahead = history.getAhead();
		var localCommit = commits.get(commits.resolve(Constants.LOCAL_BRANCH));
		var remoteCommit = commits.get(commits.resolve(Constants.REMOTE_BRANCH));
		var deleted = getDeletedInRemote(remoteCommit);
		var mergeResult = merge(localCommit, remoteCommit, deleted);
		String mergeCommitId = null;
		if (ahead.isEmpty()) {
			updateHead(remoteCommit);
		} else {
			mergeCommitId = createMergeCommit(localCommit, remoteCommit, mergeResult);
		}
		updateWorkspaceIds(remoteCommit, mergeResult, mergeCommitId);
		return mergeResult.count() > 0;
	}

	private List<Reference> getDeletedInRemote(Commit remoteCommit) throws IOException {
		var localHistory = commits.find().refs(Constants.LOCAL_REF).all();
		var remoteHistory = commits.find().refs(Constants.REMOTE_REF).all();
		var commonParent = remoteHistory.stream()
				.filter(c -> localHistory.contains(c))
				.findFirst().orElse(null);
		if (commonParent == null)
			return new ArrayList<>();
		return DiffEntries.between(git, commonParent, remoteCommit).stream()
				.filter(e -> e.getChangeType() == ChangeType.DELETE)
				.map(e -> new Reference(e.getOldPath(), commonParent.id, e.getOldId().toObjectId()))
				.collect(Collectors.toList());
	}

	private MergeResult merge(Commit localCommit, Commit remoteCommit, List<Reference> remoteDeletions) {
		var gitStore = new GitStoreReader(git, localCommit, remoteCommit, conflictResolver);
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : typeOrder) {
			var changes = gitStore.getChanges(type);
			for (var change : changes) {
				jsonImport.run(type, change.refId);
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
		return new MergeResult(gitStore.getImported(), gitStore.getMerged(), gitStore.getKeepDeleted(),
				remoteDeletions);
	}

	private <T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	private void updateWorkspaceIds(Commit remoteCommit, MergeResult result, String mergeCommitId) throws IOException {
		if (workspaceIds == null)
			return;
		result.imported.forEach(ref -> workspaceIds.put(ref.path, ref.objectId));
		result.deleted.forEach(ref -> workspaceIds.remove(ref.path));
		var merged = references.find().commit(mergeCommitId).all();
		merged.forEach(ref -> workspaceIds.put(ref.path, ref.objectId));
		updateCategoryIds(remoteCommit.id, "");
		workspaceIds.putRoot(ObjectId.fromString(remoteCommit.id));
		workspaceIds.save();
	}

	private void updateCategoryIds(String remoteCommitId, String path) {
		entries.find().commit(remoteCommitId).path(path).all().forEach(entry -> {
			if (entry.typeOfEntry == EntryType.DATASET)
				return;
			workspaceIds.put(entry.path, entry.objectId);
			updateCategoryIds(remoteCommitId, entry.path);
		});
	}

	private String createMergeCommit(Commit localCommit, Commit remoteCommit, MergeResult result)
			throws IOException {
		var config = new GitConfig(database, workspaceIds, git, committer);
		var changes = result.merged.stream()
				.map(r -> new Change(ChangeType.MODIFY, r.path))
				.collect(Collectors.toList());
		result.keepDeleted.forEach(r -> changes.add(new Change(ChangeType.DELETE, r.path)));
		result.deleted.forEach(r -> {
			if (conflictResolver.isConflict(r)
					&& conflictResolver.resolveConflict(r, null).type == ConflictResolutionType.OVERWRITE_LOCAL) {
				changes.add(new Change(ChangeType.DELETE, r.path));
			}
		});
		var commitWriter = new CommitWriter(config);
		var mergeMessage = "Merge remote-tracking branch origin/master";
		return commitWriter.mergeCommit(mergeMessage, changes, localCommit.id, remoteCommit.id);
	}

	private void updateHead(Commit commit) throws IOException {
		var update = git.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(commit.id));
		update.update();
	}

	private record MergeResult(List<Reference> imported, List<Reference> merged, List<Reference> keepDeleted,
			List<Reference> deleted) {

		private int count() {
			return imported.size() + merged.size() + deleted.size() + keepDeleted.size();
		}

	}

}
