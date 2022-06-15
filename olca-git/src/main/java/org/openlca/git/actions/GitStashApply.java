package org.openlca.git.actions;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.IDatabase;
import org.openlca.git.ObjectIdStore;

public class GitStashApply extends GitProgressAction<Void> {

	private final Repository repo;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;
	private ConflictResolver conflictResolver;
	private LibraryResolver libraryResolver;
	private IDatabase database;

	private GitStashApply(Repository repo) {
		this.repo = repo;
	}

	public static GitStashApply from(Repository repo) {
		return new GitStashApply(repo);
	}

	public GitStashApply to(IDatabase database) {
		this.database = database;
		return this;
	}

	public GitStashApply update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	public GitStashApply resolveConflictsWith(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	public GitStashApply resolveLibrariesWith(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	@Override
	public Void run() throws IOException, GitAPIException {
		if (repo == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		GitMerge.from(repo)
				.into(database)
				.as(committer)
				.update(workspaceIds)
				.resolveConflictsWith(conflictResolver)
				.resolveLibrariesWith(libraryResolver)
				.applyStash()
				.withProgress(progressMonitor)
				.run();
		GitStashDrop.from(repo).run();
		return null;
	}

}
