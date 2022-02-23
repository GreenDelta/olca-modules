package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.IDatabase;
import org.openlca.git.GitConfig;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Diff;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.writer.CommitWriter;

public class GitCommit {

	private final FileRepository git;
	private IDatabase database;
	private List<Diff> diffs;
	private String message;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;

	private GitCommit(FileRepository git) {
		this.git = git;
	}

	public static GitCommit to(FileRepository git) {
		return new GitCommit(git);
	}

	public GitCommit diffs(List<Diff> diffs) {
		this.diffs = diffs;
		return this;
	}

	public GitCommit diffs(IDatabase database) {
		this.database = database;
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

	public GitCommit update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	public void run() throws IOException {
		var config = new GitConfig(database, workspaceIds, git, committer);
		if (diffs == null) {
			diffs = getWorkspaceDiffs(config);
		}
		var writer = new CommitWriter(config);
		writer.commit(message, diffs);
	}

	private List<Diff> getWorkspaceDiffs(GitConfig config) throws IOException {
		var commit = Commits.of(git).head();
		var leftCommitId = commit != null ? commit.id : null;
		return DiffEntries.workspace(config, commit).stream()
				.map(e -> new Diff(e, leftCommitId, null))
				.toList();
	}
}
