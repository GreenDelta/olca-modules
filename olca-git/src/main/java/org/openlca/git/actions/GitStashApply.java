package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.repo.ClientRepository;

public class GitStashApply extends GitProgressAction<Void> {

	private final ClientRepository repo;
	private PersonIdent committer;
	private ConflictResolver conflictResolver;
	private DependencyResolver dependencyResolver;

	private GitStashApply(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitStashApply on(ClientRepository repo) {
		return new GitStashApply(repo);
	}

	public GitStashApply resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	public GitStashApply resolveDependenciesWith(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
		return this;
	}

	@Override
	public Void run() throws IOException, GitAPIException {
		if (repo == null || repo.database == null)
			throw new IllegalStateException("Git repository and database must be set");
		GitMerge.on(repo)
				.as(committer)
				.resolveConflictsWith(conflictResolver)
				.resolveDependenciesWith(dependencyResolver)
				.applyStash()
				.withProgress(progressMonitor)
				.run();
		GitStashDrop.from(repo).run();
		return null;
	}

}
