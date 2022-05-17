package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.IDatabase;
import org.openlca.git.GitConfig;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ImportHelper.ImportResult;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.Constants;
import org.openlca.git.util.Diffs;
import org.openlca.git.util.GitStoreReader;
import org.openlca.git.util.History;
import org.openlca.git.writer.CommitWriter;

public class GitMerge extends GitProgressAction<Boolean> {

	private final FileRepository git;
	private final History history;
	private final Commits commits;
	private IDatabase database;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;
	private ConflictResolver conflictResolver;
	private boolean applyStash;

	private GitMerge(FileRepository git) {
		this.git = git;
		this.history = History.of(git);
		this.commits = Commits.of(git);
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

	GitMerge applyStash() {
		this.applyStash = true;
		return this;
	}

	@Override
	public Boolean run() throws IOException, GitAPIException {
		if (git == null || database == null)
			throw new IllegalStateException("Git repository, database and committer, must be set");
		var behind = history.getBehind(getRef());
		if (behind.isEmpty())
			return false;
		var localCommit = commits.get(commits.resolve(Constants.LOCAL_BRANCH));
		var remoteCommit = getRemoteCommit();
		var diffs = getRemoteDiffs(remoteCommit);
		var deleted = diffs.stream()
				.filter(d -> d.diffType == DiffType.DELETED)
				.map(d -> d.toReference(Side.OLD))
				.collect(Collectors.toList());
		var addedOrChanged = diffs.stream()
				.filter(d -> d.diffType != DiffType.DELETED)
				.map(d -> d.toReference(Side.NEW))
				.collect(Collectors.toList());
		if (progressMonitor != null) {
			progressMonitor.beginTask("Merging data", addedOrChanged.size() + deleted.size());
		}
		var gitStore = new GitStoreReader(git, localCommit, remoteCommit, addedOrChanged, conflictResolver);
		var importHelper = new ImportHelper(git, database, workspaceIds, progressMonitor);
		importHelper.conflictResolver = conflictResolver;
		importHelper.runImport(gitStore);
		importHelper.delete(deleted);
		var result = new ImportResult(gitStore.getImported(), gitStore.getMerged(), gitStore.getKeepDeleted(), deleted);
		String commitId = remoteCommit.id;
		if (!applyStash) {
			var ahead = history.getAhead();
			if (ahead.isEmpty()) {
				updateHead(remoteCommit);
			} else {
				commitId = createMergeCommit(localCommit, remoteCommit, result);
			}
		}
		importHelper.updateWorkspaceIds(commitId, result, applyStash);
		return result.count() > 0;
	}

	private Commit getRemoteCommit() throws GitAPIException {
		if (!applyStash)
			return commits.get(commits.resolve(Constants.REMOTE_BRANCH));
		var commits = Git.wrap(git).stashList().call();
		if (commits == null || commits.isEmpty())
			return null;
		return new Commit(commits.iterator().next());
	}

	private List<Diff> getRemoteDiffs(Commit remoteCommit) throws IOException {
		var localHistory = commits.find().refs(Constants.LOCAL_REF).all();
		var remoteHistory = commits.find().refs(getRef()).all();
		var commonHistory = remoteHistory.stream()
				.filter(c -> localHistory.contains(c))
				.toList();
		var commonParent = !commonHistory.isEmpty()
				? commonHistory.get(commonHistory.size() - 1)
				: null;
		return Diffs.between(git, commonParent, remoteCommit);
	}

	private String getRef() {
		return applyStash ? org.eclipse.jgit.lib.Constants.R_STASH : Constants.REMOTE_REF;
	}

	private String createMergeCommit(Commit localCommit, Commit remoteCommit, ImportResult result)
			throws IOException {
		var config = new GitConfig(database, workspaceIds, git);
		var diffs = result.merged().stream()
				.map(r -> new Change(DiffType.MODIFIED, r))
				.collect(Collectors.toList());
		result.keepDeleted()
				.forEach(r -> diffs.add(new Change(DiffType.DELETED, r)));
		result.deleted().forEach(r -> {
			if (conflictResolver.isConflict(r)
					&& conflictResolver.resolveConflict(r, null).type == ConflictResolutionType.OVERWRITE_LOCAL) {
				diffs.add(new Change(DiffType.DELETED, r));
			}
		});
		var commitWriter = new CommitWriter(config, committer);
		var mergeMessage = "Merge remote-tracking branch";
		return commitWriter.mergeCommit(mergeMessage, diffs, localCommit.id, remoteCommit.id);
	}

	private void updateHead(Commit commit) throws IOException {
		var update = git.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(commit.id));
		update.update();
	}

}
