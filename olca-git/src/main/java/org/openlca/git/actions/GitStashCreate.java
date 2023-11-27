package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.Compatibility;
import org.openlca.git.Compatibility.UnsupportedClientVersionException;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.writer.DbCommitWriter;

public class GitStashCreate extends GitProgressAction<Void> {

	private final ClientRepository repo;
	private List<Change> changes;
	private PersonIdent committer;
	private Commit reference;
	private boolean discard;

	private GitStashCreate(ClientRepository repo) {
		this.repo = repo;
	}

	public static GitStashCreate on(ClientRepository repo) {
		return new GitStashCreate(repo);
	}

	public GitStashCreate changes(List<Change> changes) {
		this.changes = changes;
		return this;
	}

	public GitStashCreate as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitStashCreate reference(Commit reference) {
		this.reference = reference;
		return this;
	}

	public GitStashCreate discard() {
		this.discard = true;
		return this;
	}

	@Override
	public Void run() throws IOException {
		checkValidInputs();
		if (!discard) {
			writeStashCommit();
		}
		updateDatabase();
		return null;
	}

	private void checkValidInputs() throws UnsupportedClientVersionException {
		if (repo == null || repo.database == null)
			throw new IllegalStateException("Git repository and database must be set");
		if (!discard && committer == null)
			throw new IllegalStateException("Committer must be set");
		if (changes == null) {
			changes = repo.diffs.find().withDatabase().stream()
					.map(Change::new)
					.collect(Collectors.toList());
		}
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		Compatibility.checkRepositoryClientVersion(repo);
	}

	private void writeStashCommit() throws IOException {
		new DbCommitWriter(repo)
				.ref(Constants.R_STASH)
				.as(committer)
				.reference(reference)
				.with(progressMonitor)
				.write("Stashed changes", changes);
	}

	private void updateDatabase() throws IOException {
		var commit = reference == null
				? repo.commits.head()
				: reference;
		if (commit != null) {
			restoreDataFrom(commit);
		}
		deleteAddedData();
	}

	private void restoreDataFrom(Commit commit) {
		var toImport = changes.stream()
				.filter(c -> c.diffType != DiffType.ADDED)
				.map(c -> repo.references.get(c.type, c.refId, commit.id))
				.collect(Collectors.toList());
		var gitStore = new GitStoreReader(repo, commit, toImport);
		ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
	}

	private void deleteAddedData() {
		var toDelete = changes.stream()
				.filter(c -> c.diffType == DiffType.ADDED)
				.collect(Collectors.toList());
		DeleteData.from(repo.database)
				.with(progressMonitor)
				.data(toDelete)
				.run();
	}

}
