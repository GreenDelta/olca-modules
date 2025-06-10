package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.actions.LibraryMounter.MountException;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.Constants;
import org.openlca.git.writer.DbCommitWriter;

public class GitMerge extends GitProgressAction<MergeResult> {

	private final ClientRepository repo;
	private PersonIdent committer;
	private ConflictResolver conflictResolver = ConflictResolver.NULL;
	private LibraryResolver libraryResolver;
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

	public GitMerge as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitMerge resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		return this;
	}

	public GitMerge resolveLibrariesWith(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	GitMerge applyStash() {
		this.applyStash = true;
		return this;
	}

	@Override
	public MergeResult run() throws IOException, GitAPIException {
		if (!prepare())
			return MergeResult.NO_CHANGES;
		List<Diff> merged = null;
		try {
			merged = Data.of(repo, localCommit, remoteCommit)
					.with(conflictResolver)
					.with(libraryResolver)
					.with(progressMonitor)
					.changes(changes)
					.update();
			if (merged == null)
				return MergeResult.ABORTED;
		} catch (MountException e) {
			return MergeResult.MOUNT_ERROR;
		}
		if (applyStash)
			return MergeResult.SUCCESS;
		var ahead = repo.localHistory.getAheadOf(Constants.REMOTE_REF);
		if (ahead.isEmpty()) {
			updateHead();
		} else {
			createMergeCommit(merged);
		}
		return MergeResult.SUCCESS;
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
		remoteCommit = getRemoteCommit();
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

	private String createMergeCommit(List<Diff> merged) throws IOException {
		var localLibs = repo.getLibraries(localCommit);
		var remoteLibs = repo.getLibraries(remoteCommit);
		if (!localLibs.equals(remoteLibs)) {
			merged.add(Diff.modified(
					repo.references.get(RepositoryInfo.FILE_NAME, remoteCommit.id),
					new Reference(RepositoryInfo.FILE_NAME)));
		}
		return new DbCommitWriter(repo)
				.as(committer)
				.merge(localCommit.id, remoteCommit.id)
				.write("Merge remote-tracking branch", merged);
	}

	private void updateHead() throws IOException {
		var update = repo.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommit.id));
		update.update();
		progressMonitor.beginTask("Updating local index");
		repo.index.reload();
	}

	public static enum MergeResult {

		NO_CHANGES, SUCCESS, MOUNT_ERROR, ABORTED;

	}

}
