package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.git.Compatibility;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
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
		var commit = reference == null ? repo.commits.head() : reference;
		var toDelete = changes.stream()
				.filter(c -> c.diffType == DiffType.ADDED)
				.collect(Collectors.toList());
		var toImport = commit != null
				? changes.stream()
						.filter(c -> c.diffType != DiffType.ADDED)
						.map(c -> repo.references.get(c.type, c.refId, commit.id))
						.collect(Collectors.toList())
				: new ArrayList<Reference>();
		progressMonitor.beginTask("Stashing data", changes.size() + toDelete.size() + toImport.size());
		if (!discard) {
			var writer = new DbCommitWriter(repo)
					.ref(Constants.R_STASH)
					.as(committer)
					.reference(reference)
					.with(progressMonitor);
			writer.write("Stashed changes", changes);
		}
		var importHelper = new ImportHelper(repo, progressMonitor);
		if (commit == null) {
			importHelper.delete(toDelete);
		} else {
			var gitStore = new GitStoreReader(repo, commit, toImport);
			importHelper.runImport(gitStore);
			importHelper.delete(toDelete);
		}
		return null;
	}

}
