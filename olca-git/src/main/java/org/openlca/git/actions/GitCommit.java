package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.IDatabase;
import org.openlca.git.Compatibility;
import org.openlca.git.GitIndex;
import org.openlca.git.find.Diffs;
import org.openlca.git.model.Change;
import org.openlca.git.util.Descriptors;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Strings;

public class GitCommit extends GitProgressAction<String> {

	private final IDatabase database;
	private Repository repo;
	private List<Change> changes;
	private String message;
	private GitIndex gitIndex;
	private PersonIdent committer;

	private GitCommit(IDatabase database) {
		this.database = database;
	}

	public static GitCommit from(IDatabase database) {
		return new GitCommit(database);
	}

	public GitCommit to(Repository repo) {
		this.repo = repo;
		return this;
	}

	public GitCommit changes(List<Change> changes) {
		this.changes = changes;
		return this;
	}

	public GitCommit as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitCommit withMessage(String message) {
		this.message = message;
		return this;
	}

	public GitCommit update(GitIndex gitIndex) {
		this.gitIndex = gitIndex;
		return this;
	}

	@Override
	public String run() throws IOException {
		if (repo == null || database == null || Strings.nullOrEmpty(message))
			throw new IllegalStateException("Git repository, database and message must be set");
		if (changes == null) {
			if (gitIndex == null)
				throw new IllegalStateException("gitIndex must be set when no changes are specified");
			changes = Diffs.of(repo).with(database, gitIndex).stream()
					.map(Change::new)
					.collect(Collectors.toList());
		}
		Compatibility.checkRepositoryClientVersion(repo);
		progressMonitor.beginTask("Writing commit", changes.size());
		var writer = new DbCommitWriter(repo, database, Descriptors.of(database))
				.update(gitIndex)
				.as(committer)
				.with(progressMonitor);
		var commitId = writer.write(message, changes);
		return commitId;
	}

}
