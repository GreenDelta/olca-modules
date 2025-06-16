package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.repo.ClientRepository;

public class GitReset extends GitProgressAction<String> {

	protected final ClientRepository repo;
	protected DependencyResolver dependencyResolver;
	protected Commit commit;
	protected List<Diff> changes;

	protected GitReset(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitReset on(ClientRepository repo) {
		return new GitReset(repo);
	}

	public GitReset resolveDependenciesWith(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
		return this;
	}

	public GitReset to(Commit commit) {
		this.commit = commit;
		return this;
	}

	public GitReset changes(List<Diff> changes) {
		this.changes = changes;
		return this;
	}

	@Override
	public String run() throws IOException, GitAPIException {
		checkValidInputs();
		Data.of(repo, commit)
				.with(dependencyResolver)
				.with(progressMonitor)
				.changes(changes)
				.undo()
				.update();
		return null;
	}

	protected void checkValidInputs() throws UnsupportedClientVersionException {
		if (repo == null || repo.database == null)
			throw new IllegalStateException("Git repository and database must be set");
		if (commit == null) {
			commit = repo.commits.head();
		}
		if (changes == null) {
			changes = repo.diffs.find().commit(commit).withDatabase();
		}
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		Compatibility.checkRepositoryClientVersion(repo);
	}

}
