package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.Compatibility;
import org.openlca.git.model.Change;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Strings;

public class GitCommit extends GitProgressAction<String> {

	private ClientRepository repo;
	private List<Change> changes;
	private String message;
	private PersonIdent committer;

	private GitCommit(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitCommit on(ClientRepository repo) {
		return new GitCommit(repo);
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

	@Override
	public String run() throws IOException {
		if (repo == null || repo.database == null || Strings.nullOrEmpty(message))
			throw new IllegalStateException("Git repository, database and message must be set");
		if (changes == null) {
			changes = repo.diffs.find().withDatabase().stream()
					.map(Change::new)
					.collect(Collectors.toList());
		}
		Compatibility.checkRepositoryClientVersion(repo);
		progressMonitor.beginTask("Writing commit", changes.size());
		var writer = new DbCommitWriter(repo)
				.as(committer)
				.with(progressMonitor);
		var commitId = writer.write(message, changes);
		repo.index.reload();
		return commitId;
	}

}
