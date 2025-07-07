package org.openlca.git.actions;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.DataPackage;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.Constants;
import org.openlca.git.writer.DbCommitWriter;

public class GitMerge extends GitProgressAction<MergeResult> {

	private final ClientRepository repo;
	private PersonIdent committer;
	private DataPackage dataPackage;
	private ConflictResolver conflictResolver = ConflictResolver.NULL;
	private DependencyResolver dependencyResolver;
	private boolean applyStash;
	private Commit localCommit;
	private Commit remoteCommit;
	private List<Diff> changes;

	private GitMerge(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitMerge on(ClientRepository repo) {
		return new GitMerge(repo);
	}

	public GitMerge into(DataPackage dataPackage) {
		this.dataPackage = dataPackage;
		return this;
	}

	public GitMerge commit(Commit commit) {
		this.remoteCommit = commit;
		return this;
	}

	public GitMerge as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitMerge resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		return this;
	}

	public GitMerge resolveDependenciesWith(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
		return this;
	}

	GitMerge applyStash() {
		this.applyStash = true;
		return this;
	}

	@Override
	public MergeResult run() throws IOException, GitAPIException {
		if (!prepare())
			return new MergeResult(MergeResultType.NO_CHANGES);
		var updateResult = Data.of(repo, localCommit, remoteCommit)
				.with(conflictResolver)
				.with(dependencyResolver)
				.with(progressMonitor)
				.into(dataPackage)
				.changes(changes)
				.update();
		var mergeResultType = updateResult.mergeResult().type();
		if (mergeResultType == MergeResultType.ABORTED || mergeResultType == MergeResultType.MOUNT_ERROR)
			return updateResult.mergeResult();
		if (applyStash)
			return new MergeResult(MergeResultType.SUCCESS, updateResult.mergeResult().mountedDataPackages());
		var ahead = repo.localHistory.getAheadOf(Constants.REMOTE_REF);
		if (ahead.isEmpty()) {
			updateHead();
		} else {
			createMergeCommit(updateResult.mergedData());
		}
		return new MergeResult(MergeResultType.SUCCESS, updateResult.mergeResult().mountedDataPackages());
	}

	private boolean prepare() throws GitAPIException, UnsupportedClientVersionException {
		if (repo == null || repo.database == null)
			throw new IllegalStateException("Git repository and database must be set");
		Compatibility.checkRepositoryClientVersion(repo);
		var ref = applyStash
				? org.eclipse.jgit.lib.Constants.R_STASH
				: Constants.REMOTE_REF;
		var behind = repo.localHistory.getBehindOf(ref);
		if (behind.isEmpty())
			return false;
		localCommit = repo.commits.get(repo.commits.resolve(Constants.LOCAL_BRANCH));
		if (remoteCommit == null) {
			remoteCommit = getRemoteCommit();
		}
		if (remoteCommit == null)
			return false;
		var commonParent = repo.localHistory.commonParentOf(ref);
		changes = repo.diffs.find().commit(commonParent).with(remoteCommit);
		return true;
	}

	private Commit getRemoteCommit() throws GitAPIException {
		if (!applyStash)
			return repo.commits.get(repo.commits.resolve(Constants.REMOTE_BRANCH));
		return repo.commits.stash();
	}

	private String createMergeCommit(MergedData mergedData) throws IOException {
		var localLibs = repo.getDataPackages(localCommit);
		var remoteLibs = repo.getDataPackages(remoteCommit);
		var diffs = mergedData.getDiffs();
		if (!localLibs.equals(remoteLibs)) {
			// TODO is this correct?
			diffs.add(Diff.modified(
					repo.references.get(RepositoryInfo.FILE_NAME, remoteCommit.id),
					new Reference(RepositoryInfo.FILE_NAME)));
		}
		return new DbCommitWriter(repo)
				.as(committer)
				.merge(localCommit.id, remoteCommit.id, mergedData)
				.write("Merge remote-tracking branch", diffs);
	}

	private void updateHead() throws IOException {
		var update = repo.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommit.id));
		update.update();
		progressMonitor.beginTask("Updating local index");
		repo.index.reload();
	}

	public static record MergeResult(MergeResultType type, Set<DataPackage> mountedDataPackages) {

		public MergeResult(MergeResultType type) {
			this(type, new HashSet<>());
		}

	}

	public static enum MergeResultType {

		NO_CHANGES, SUCCESS, MOUNT_ERROR, ABORTED;

	}

}
