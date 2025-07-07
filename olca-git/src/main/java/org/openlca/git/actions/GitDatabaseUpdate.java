package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openlca.git.model.Diff;
import org.openlca.git.repo.ClientRepository;

public class GitDatabaseUpdate extends GitProgressAction<List<Diff>> {

	private final ClientRepository repo;
	private DependencyResolver dependencyResolver;

	private GitDatabaseUpdate(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitDatabaseUpdate on(ClientRepository repo) {
		return new GitDatabaseUpdate(repo);
	}

	public GitDatabaseUpdate resolveDependenciesWith(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
		return this;
	}

	public List<Diff> run() throws IOException, GitAPIException {
		if (repo == null)
			throw new IllegalStateException("Git repository must be set");
		return Data.of(repo, repo.commits.head())
				.changes(repo.diffs.find().withDatabase().stream().map(Diff::flip).toList())
				.with(dependencyResolver)
				.with(progressMonitor)
				.update()
				.mergedData()
				.getDiffs();
	}

}
