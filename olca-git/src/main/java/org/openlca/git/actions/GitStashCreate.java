package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.writer.DbCommitWriter;

public class GitStashCreate extends GitReset {

	private PersonIdent committer;
	private Commit parent;

	private GitStashCreate(ClientRepository repo) {
		super(repo);
	}

	public static GitStashCreate on(ClientRepository repo) {
		return new GitStashCreate(repo);
	}

	@Override
	public GitStashCreate changes(List<Diff> changes) {
		super.changes(changes);
		return this;
	}

	public GitStashCreate as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitStashCreate parent(Commit parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public String run() throws IOException, GitAPIException {
		checkValidInputs();
		var commitId = writeStashCommit();
		super.run();
		return commitId;
	}

	@Override
	protected void checkValidInputs() throws UnsupportedClientVersionException {
		super.checkValidInputs();
		if (committer == null)
			throw new IllegalStateException("Committer must be set");
	}

	private String writeStashCommit() throws IOException {
		var parent = this.parent != null
				? repo.parseCommit(ObjectId.fromString(this.parent.id))
				: null;
		return new DbCommitWriter(repo)
				.ref(Constants.R_STASH)
				.as(committer)
				.parent(parent)
				.with(progressMonitor)
				.write("Stashed changes", changes);
	}

}
