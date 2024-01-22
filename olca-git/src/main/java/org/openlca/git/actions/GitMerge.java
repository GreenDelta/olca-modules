package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.git.Compatibility;
import org.openlca.git.GitIndex;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ImportResults.ImportState;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.Constants;
import org.openlca.git.util.Descriptors;
import org.openlca.git.util.Diffs;
import org.openlca.git.util.History;
import org.openlca.git.util.Repositories;
import org.openlca.git.writer.DbCommitWriter;

public class GitMerge extends GitProgressAction<Boolean> {

	private final Repository repo;
	private final History localHistory;
	private final Commits commits;
	private IDatabase database;
	private Descriptors descriptors;
	private GitIndex gitIndex;
	private PersonIdent committer;
	private ConflictResolver conflictResolver = ConflictResolver.NULL;
	private LibraryResolver libraryResolver;
	private boolean applyStash;

	private GitMerge(Repository repo) {
		this.repo = repo;
		this.localHistory = History.localOf(repo);
		this.commits = Commits.of(repo);
	}

	public static GitMerge from(Repository repo) {
		return new GitMerge(repo);
	}

	public GitMerge into(IDatabase database) {
		this.database = database;
		this.descriptors = Descriptors.of(database);
		return this;
	}

	public GitMerge update(GitIndex gitIndex) {
		this.gitIndex = gitIndex;
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

	public GitMerge resolveLibrariesWith(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	GitMerge applyStash() {
		this.applyStash = true;
		return this;
	}

	@Override
	public Boolean run() throws IOException, GitAPIException {
		if (repo == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		var behind = localHistory.getBehindOf(getRef());
		if (behind.isEmpty())
			return false;
		var localCommit = commits.get(commits.resolve(Constants.LOCAL_BRANCH));
		var remoteCommit = getRemoteCommit();
		if (remoteCommit == null)
			return false;
		var toMount = resolveLibraries(remoteCommit);
		if (toMount == null)
			return null;
		Compatibility.checkRepositoryClientVersion(repo);
		var commonParent = localHistory.commonParentOf(getRef());
		var diffs = Diffs.of(repo, commonParent).with(remoteCommit);
		var deleted = diffs.stream()
				.filter(d -> d.diffType == DiffType.DELETED)
				.map(d -> d.toReference(Side.OLD))
				.collect(Collectors.toList());
		var addedOrChanged = diffs.stream()
				.filter(d -> d.diffType != DiffType.DELETED)
				.map(d -> d.toReference(Side.NEW))
				.collect(Collectors.toList());
		var ahead = !applyStash
				? localHistory.getAheadOf(Constants.REMOTE_REF)
				: new ArrayList<>();
		var work = toMount.size() + addedOrChanged.size() + deleted.size() + (!ahead.isEmpty() ? 1 : 0);
		progressMonitor.beginTask("Merging data", work);
		if (!mountLibraries(toMount))
			throw new IOException("Could not mount libraries");
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, addedOrChanged, conflictResolver);
		var importHelper = new ImportHelper(repo, database, descriptors, gitIndex, progressMonitor);
		importHelper.conflictResolver = conflictResolver;
		importHelper.delete(deleted);
		importHelper.runImport(gitStore);
		// TODO unmount libs removed from package info; not yet supported
		var result = gitStore.getResults();
		deleted.forEach(ref -> result.add(ref, ImportState.DELETED));
		String commitId = remoteCommit.id;
		if (!applyStash) {
			if (ahead.isEmpty()) {
				updateHead(remoteCommit);
			} else {
				commitId = createMergeCommit(localCommit, remoteCommit, result);
			}
		}
		importHelper.updateGitIndex(commitId, result, applyStash);
		return result.size() > 0;
	}

	private Commit getRemoteCommit() throws GitAPIException {
		if (!applyStash)
			return commits.get(commits.resolve(Constants.REMOTE_BRANCH));
		var commits = Git.wrap(repo).stashList().call();
		if (commits == null || commits.isEmpty())
			return null;
		return new Commit(commits.iterator().next());
	}

	private String getRef() {
		return applyStash ? org.eclipse.jgit.lib.Constants.R_STASH : Constants.REMOTE_REF;
	}

	private String createMergeCommit(Commit localCommit, Commit remoteCommit, ImportResults result)
			throws IOException {
		var diffs = result.get(ImportState.MERGED).stream()
				.map(r -> new Change(DiffType.MODIFIED, r))
				.collect(Collectors.toList());
		result.get(ImportState.KEPT).forEach(r -> diffs.add(new Change(DiffType.MODIFIED, r)));
		result.get(ImportState.KEPT_DELETED).forEach(r -> diffs.add(new Change(DiffType.DELETED, r)));
		result.get(ImportState.DELETED).forEach(r -> {
			if (conflictResolver.isConflict(r)
					&& conflictResolver.resolveConflict(r, null).type == ConflictResolutionType.OVERWRITE) {
				diffs.add(new Change(DiffType.DELETED, r));
			}
		});
		progressMonitor.subTask("Writing merged changes");
		var writer = new DbCommitWriter(repo, database, descriptors)
				.update(gitIndex)
				.as(committer)
				.merge(localCommit.id, remoteCommit.id);
		var commitId = writer.write("Merge remote-tracking branch", diffs);
		progressMonitor.worked(1);
		return commitId;
	}

	private void updateHead(Commit commit) throws IOException {
		var update = repo.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(commit.id));
		update.update();
	}

	private List<Library> resolveLibraries(Commit commit) {
		var info = Repositories.infoOf(repo, commit);
		if (info == null)
			return new ArrayList<>();
		if (libraryResolver == null)
			return null;
		var remoteLibs = info.libraries();
		var localLibs = database.getLibraries();
		var libs = new ArrayList<Library>();
		for (var newLib : remoteLibs) {
			if (localLibs.contains(newLib.id()))
				continue;
			var lib = libraryResolver.resolve(newLib.id());
			if (lib == null)
				return null;
			libs.add(lib);
		}
		return libs;
	}

	private boolean mountLibraries(List<Library> newLibraries) {
		var queue = new ArrayDeque<>(newLibraries);
		var handled = new HashSet<Library>();
		while (!queue.isEmpty()) {
			var next = queue.poll();
			progressMonitor.subTask("Mounting library " + next.name());
			if (handled.contains(next)) {
				progressMonitor.worked(1);
				continue;
			}
			handled.add(next);

			// run a pre-mount check; transitive dependencies will
			// be handled as well; we apply the default action
			// related to the library states of the check result
			// when mounting these libraries
			var checkResult = PreMountCheck.check(database, next);
			if (checkResult.isError())
				return false; // might result in partly mounted library list
			checkResult.getStates().forEach(p -> handled.add(p.first));
			Mounter.of(database, next)
					.applyDefaultsOf(checkResult)
					.run();
			progressMonitor.worked(1);
		}
		return true;
	}

}
