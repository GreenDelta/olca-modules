package org.openlca.git.actions;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.model.Commit;
import org.openlca.git.repo.ClientRepository;

public class GitDiscard extends GitProgressAction<String> {

	protected final ClientRepository repo;
	protected LibraryResolver libraryResolver;
	protected Set<Change> changes;

	protected GitDiscard(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitDiscard on(ClientRepository repo) {
		return new GitDiscard(repo);
	}

	public GitDiscard resolveLibrariesWith(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	public GitDiscard changes(Set<Change> changes) {
		this.changes = changes;
		return this;
	}

	@Override
	public String run() throws IOException {
		checkValidInputs();
		updateDatabase();
		progressMonitor.beginTask("Reloading descriptors");
		repo.descriptors.reload();
		return null;
	}

	protected void checkValidInputs() throws UnsupportedClientVersionException {
		if (repo == null || repo.database == null)
			throw new IllegalStateException("Git repository and database must be set");
		if (changes == null) {
			changes = Change.of(repo.diffs.find().withDatabase());
		}
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		Compatibility.checkRepositoryClientVersion(repo);
	}

	private void updateDatabase() throws IOException {
		var commit = repo.commits.head();
		var libraries = LibraryMounter.of(repo, commit)
				.with(libraryResolver)
				.with(progressMonitor);
		libraries.mountNew();
		if (commit != null) {
			restoreDataFrom(commit);
		}
		deleteAddedData();
		libraries.unmountObsolete();
	}

	private void restoreDataFrom(Commit commit) {
		var toImport = changes.stream()
				.filter(c -> c.changeType != ChangeType.ADD)
				.map(c -> repo.references.get(c.type, c.refId, commit.id))
				.collect(Collectors.toList());
		var gitStore = new GitStoreReader(repo, commit, toImport);
		ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
	}

	private void deleteAddedData() {
		var toDelete = changes.stream()
				.filter(c -> c.changeType == ChangeType.ADD)
				.collect(Collectors.toList());
		DeleteData.from(repo.database)
				.with(progressMonitor)
				.data(toDelete)
				.run();
	}

}
