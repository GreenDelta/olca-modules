package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.openlca.git.model.Diff;
import org.openlca.git.repo.ClientRepository;

public class GitDatabaseUpdate extends GitProgressAction<List<Diff>> {

	private final ClientRepository repo;
	private LibraryResolver libraryResolver;

	private GitDatabaseUpdate(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitDatabaseUpdate on(ClientRepository repo) {
		return new GitDatabaseUpdate(repo);
	}

	public GitDatabaseUpdate resolveLibrariesWith(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	public List<Diff> run() throws IOException {
		if (repo == null)
			throw new IllegalStateException("Git repository must be set");
		return Data.of(repo, repo.commits.head())
				.changes(repo.diffs.find().databaseWithCommit())
				.with(libraryResolver)
				.with(progressMonitor)
				.update();
	}

}
