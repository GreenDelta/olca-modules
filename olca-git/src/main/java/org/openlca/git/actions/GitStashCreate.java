package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.IDatabase;
import org.openlca.git.Compatibility;
import org.openlca.git.GitIndex;
import org.openlca.git.actions.ImportResults.ImportState;
import org.openlca.git.find.Commits;
import org.openlca.git.find.References;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Descriptors;
import org.openlca.git.util.Diffs;
import org.openlca.git.writer.DbCommitWriter;

public class GitStashCreate extends GitProgressAction<Void> {

	private final IDatabase database;
	private List<Change> changes;
	private Repository repo;
	private Commits commits;
	private References references;
	private GitIndex gitIndex;
	private PersonIdent committer;
	private Commit reference;
	private boolean discard;

	private GitStashCreate(IDatabase database) {
		this.database = database;
	}

	public static GitStashCreate from(IDatabase database) {
		return new GitStashCreate(database);
	}

	public GitStashCreate changes(List<Change> changes) {
		this.changes = changes;
		return this;
	}

	public GitStashCreate to(Repository repo) {
		this.repo = repo;
		this.commits = Commits.of(repo);
		this.references = References.of(repo);
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

	public GitStashCreate update(GitIndex gitIndex) {
		this.gitIndex = gitIndex;
		return this;
	}

	@Override
	public Void run() throws IOException {
		if (repo == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		if (changes == null && gitIndex == null)
			throw new IllegalStateException("Either changes or gitIndex must be set");
		if (!discard && committer == null)
			throw new IllegalStateException("Committer must be set");
		if (changes == null) {
			changes = Diffs.of(repo).with(database, gitIndex).stream()
					.map(Change::new)
					.collect(Collectors.toList());
		}
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		Compatibility.checkRepositoryClientVersion(repo);
		var commit = reference == null ? commits.head() : reference;
		var toDelete = changes.stream()
				.filter(c -> c.diffType == DiffType.ADDED)
				.collect(Collectors.toList());
		var toImport = commit != null
				? changes.stream()
						.filter(c -> c.diffType != DiffType.ADDED)
						.map(c -> references.get(c.type, c.refId, commit.id))
						.collect(Collectors.toList())
				: new ArrayList<Reference>();
		var descriptors = Descriptors.of(database);
		progressMonitor.beginTask("Stashing data", changes.size() + toDelete.size() + toImport.size());
		if (!discard) {
			var writer = new DbCommitWriter(repo, database, descriptors)
					.ref(Constants.R_STASH)
					.update(gitIndex)
					.as(committer)
					.reference(reference)
					.with(progressMonitor);
			writer.write("Stashed changes", changes);
		}
		var importHelper = new ImportHelper(repo, database, descriptors, gitIndex, progressMonitor);
		if (commit == null) {
			importHelper.delete(toDelete);
			if (gitIndex != null) {
				gitIndex.clear();
			}
		} else {
			var gitStore = new GitStoreReader(repo, commit, toImport);
			importHelper.runImport(gitStore);
			importHelper.delete(toDelete);
			var result = gitStore.getResults();
			toDelete.forEach(ref -> result.add(ref, ImportState.DELETED));
			importHelper.updateGitIndex(commit.id, result, false);
		}
		return null;
	}

}
