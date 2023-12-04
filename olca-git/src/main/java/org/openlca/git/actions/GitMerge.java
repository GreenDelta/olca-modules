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
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.library.Unmounter;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.Constants;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.jsonld.LibraryLink;

public class GitMerge extends GitProgressAction<MergeResult> {

	private final ClientRepository repo;
	private PersonIdent committer;
	private ConflictResolver conflictResolver = ConflictResolver.NULL;
	private LibraryResolver libraryResolver;
	private boolean applyStash;
	private Commit localCommit;
	private Commit remoteCommit;
	private List<Diff> diffs;
	private List<Change> mergeResults = new ArrayList<>();

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
		var mountResult = mountLibraries();
		if (mountResult == MergeResult.MOUNT_ERROR || mountResult == MergeResult.ABORTED)
			return mountResult;
		importData();
		deleteData();
		unmountLibraries();
		if (applyStash)
			return MergeResult.SUCCESS;
		var ahead = repo.localHistory.getAheadOf(Constants.REMOTE_REF);
		if (ahead.isEmpty()) {
			updateHead();
		} else {
			createMergeCommit();
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
		diffs = repo.diffs.find().commit(commonParent).with(remoteCommit);
		return true;
	}

	private void importData() {
		var addedOrChanged = diffs.stream()
				.filter(d -> d.diffType != DiffType.DELETED)
				.map(d -> d.toReference(Side.NEW))
				.collect(Collectors.toList());
		if (addedOrChanged.isEmpty())
			return;
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, addedOrChanged, conflictResolver);
		var mergeResults = ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
		this.mergeResults.addAll(mergeResults);
	}

	private void deleteData() {
		var deleted = diffs.stream()
				.filter(d -> d.diffType == DiffType.DELETED)
				.map(d -> d.toReference(Side.OLD))
				.collect(Collectors.toList());
		if (deleted.isEmpty())
			return;
		var mergeResults = DeleteData.from(repo.database)
				.with(progressMonitor)
				.with(conflictResolver)
				.data(deleted)
				.run();
		this.mergeResults.addAll(mergeResults);
	}

	private Commit getRemoteCommit() throws GitAPIException {
		if (!applyStash)
			return repo.commits.get(repo.commits.resolve(Constants.REMOTE_BRANCH));
		var commits = Git.wrap(repo).stashList().call();
		if (commits == null || commits.isEmpty())
			return null;
		return new Commit(commits.iterator().next());
	}

	private String createMergeCommit()
			throws IOException {
		return new DbCommitWriter(repo)
				.as(committer)
				.merge(localCommit.id, remoteCommit.id)
				.write("Merge remote-tracking branch", mergeResults);
	}

	private void updateHead() throws IOException {
		var update = repo.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommit.id));
		update.update();
		repo.index.reload();
	}

	private MergeResult mountLibraries() {
		var newLibraries = resolveNewLibraries();
		if (newLibraries.size() == 0)
			return MergeResult.NO_CHANGES;
		progressMonitor.beginTask("Mounting libraries");
		var queue = new ArrayDeque<>(newLibraries);
		var handled = new HashSet<Library>();
		while (!queue.isEmpty()) {
			var next = queue.poll();
			progressMonitor.subTask(next.name());
			if (handled.contains(next))
				continue;
			handled.add(next);
			var checkResult = PreMountCheck.check(repo.database, next);
			if (checkResult.isError())
				return MergeResult.MOUNT_ERROR;
			checkResult.getStates().forEach(p -> handled.add(p.first));
			Mounter.of(repo.database, next)
					.applyDefaultsOf(checkResult)
					.run();
		}
		return MergeResult.SUCCESS;
	}

	private List<Library> resolveNewLibraries() {
		var info = repo.getInfo(remoteCommit);
		if (info == null)
			return new ArrayList<>();
		if (libraryResolver == null)
			throw new IllegalStateException("Could not mount libraries because no library resolver was set");
		var remoteLibs = info.libraries().stream().map(LibraryLink::id).toList();
		var localLibs = repo.database.getLibraries();
		var libs = new ArrayList<Library>();
		for (var remoteLib : remoteLibs) {
			if (localLibs.contains(remoteLib))
				continue;
			var lib = libraryResolver.resolve(remoteLib);
			if (lib == null)
				return null;
			libs.add(lib);
		}
		return libs;
	}

	private void unmountLibraries() {
		var libraries = resolveObsoleteLibraries();
		var unmounter = new Unmounter(repo.database);
		for (var lib : libraries) {
			unmounter.unmountUnsafe(lib);
		}
	}

	private List<String> resolveObsoleteLibraries() {
		var info = repo.getInfo(remoteCommit);
		if (info == null)
			return new ArrayList<>();
		var remoteLibs = info.libraries().stream().map(LibraryLink::id).toList();
		var localLibs = repo.database.getLibraries();
		var libs = new ArrayList<String>();
		for (var localLib : localLibs) {
			if (remoteLibs.contains(localLib))
				continue;
			libs.add(localLib);
		}
		return libs;
	}

	public static enum MergeResult {

		NO_CHANGES, SUCCESS, MOUNT_ERROR, ABORTED;

	}

}
