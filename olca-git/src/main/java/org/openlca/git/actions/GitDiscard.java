package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.ClientRepository;

public class GitDiscard extends GitProgressAction<String> {

	protected final ClientRepository repo;
	protected LibraryResolver libraryResolver;
	protected List<Diff> changes;

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

	public GitDiscard changes(List<Diff> changes) {
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
			changes = repo.diffs.find().withDatabase();
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
		var data = Data.of(repo, commit)
				.changes(changes)
				.with(progressMonitor);
		if (commit != null) {
			data.doImport(c -> c.diffType != DiffType.ADDED);
		}
		data.doDelete(c -> c.diffType == DiffType.ADDED);
		libraries.unmountObsolete();
	}

}
