package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.library.Unmounter;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.Constants;
import org.openlca.git.util.ModelRefSet;
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
	private RepositoryInfo info;
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
		var imported = importData();
		deleteData(imported);
		unmountLibraries();
		progressMonitor.beginTask("Reloading descriptors");
		repo.descriptors.reload();
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
		info = localCommit != null
				? repo.getInfo(localCommit).merge(repo.getInfo(remoteCommit))
				: repo.getInfo(remoteCommit);
		return true;
	}

	private List<Reference> importData() {
		var addedOrChanged = diffs.stream()
				.filter(d -> d.diffType != DiffType.DELETED)
				.map(d -> d.newRef)
				.collect(Collectors.toList());
		if (addedOrChanged.isEmpty())
			return addedOrChanged;
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, addedOrChanged, conflictResolver);
		var mergeResults = ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
		this.mergeResults.addAll(mergeResults);
		return addedOrChanged;
	}

	private void deleteData(List<Reference> imported) {
		// if a dataset was added and deleted it means that it was moved, the
		// add will result in an updated dataset when during importData() so
		// skip the delete in these cases
		var dontDelete = new ModelRefSet(imported);
		var deleted = diffs.stream()
				.filter(d -> d.diffType == DiffType.DELETED)
				.filter(d -> !dontDelete.contains(d))
				.map(d -> d.oldRef)
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
		return repo.commits.stash();
	}

	private String createMergeCommit() throws IOException {
		return new DbCommitWriter(repo)
				.as(committer)
				.merge(localCommit.id, remoteCommit.id)
				.write("Merge remote-tracking branch", mergeResults);
	}

	private void updateHead() throws IOException {
		var update = repo.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommit.id));
		update.update();
		progressMonitor.beginTask("Updating local index");
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
		if (info == null)
			return new ArrayList<>();
		var remoteLibs = info.libraries().stream().map(LibraryLink::id).toList();
		var localLibs = repo.database.getLibraries();
		var libs = new ArrayList<Library>();
		for (var remoteLib : remoteLibs) {
			if (localLibs.contains(remoteLib))
				continue;
			if (libraryResolver == null)
				throw new IllegalStateException("Could not mount libraries because no library resolver was set");
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
