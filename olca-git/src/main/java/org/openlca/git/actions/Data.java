package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.actions.LibraryMounter.MountException;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.ProgressMonitor;

class Data {

	private ClientRepository repo;
	private Commit localCommit;
	private Commit remoteCommit;
	private List<Diff> changes;
	/* If true ADDED will be treated as DELETED and DELETED as ADDED */
	private boolean undo;
	private ProgressMonitor progressMonitor;
	private LibraryResolver libraryResolver;
	private ConflictResolver conflictResolver;

	static Data of(ClientRepository repo, Commit remoteCommit) {
		return of(repo, null, remoteCommit);
	}

	static Data of(ClientRepository repo, Commit localCommit, Commit remoteCommit) {
		var data = new Data();
		data.repo = repo;
		data.localCommit = localCommit;
		data.remoteCommit = remoteCommit;
		return data;
	}

	Data changes(List<Diff> changes) {
		this.changes = changes;
		return this;
	}

	Data undo() {
		this.undo = true;
		return this;
	}

	Data with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	Data with(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	Data with(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	List<Diff> update() throws MountException {
		var merged = new ArrayList<Diff>();
		var libraries = LibraryMounter.of(repo, localCommit, remoteCommit)
				.with(libraryResolver)
				.with(progressMonitor);
		var mountResult = libraries.mountNew();
		if (mountResult == MergeResult.ABORTED)
			return null;
		merged.addAll(doImport());
		merged.addAll(doDelete());
		libraries.unmountObsolete();
		progressMonitor.beginTask("Reloading descriptors");
		repo.descriptors.reload();
		return merged;
	}

	private List<Diff> doImport() {
		if (remoteCommit == null)
			return new ArrayList<>();
		var toImport = changes.stream()
				.filter(diff -> !diff.isLibrary)
				.filter(diff -> diff.diffType != (undo ? DiffType.ADDED : DiffType.DELETED))
				.map(diff -> undo ? diff.oldRef : diff.newRef)
				.collect(Collectors.toList());
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, toImport, conflictResolver);
		return ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
	}

	private List<Diff> doDelete() {
		var toDelete = changes.stream()
				.filter(diff -> !diff.isLibrary)
				.filter(diff -> diff.diffType == (undo ? DiffType.ADDED : DiffType.DELETED))
				.map(diff -> undo ? diff.newRef : diff.oldRef)
				.collect(Collectors.toList());
		if (toDelete.isEmpty())
			return new ArrayList<>();
		return DeleteData.from(repo.database)
				.with(progressMonitor)
				.with(conflictResolver)
				.data(toDelete)
				.run();
	}

}
