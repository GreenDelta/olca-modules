package org.openlca.git.actions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.git.GitConfig;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.actions.ImportHelper.ImportResult;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Diffs;
import org.openlca.git.util.GitStoreReader;
import org.openlca.git.writer.CommitWriter;

public class GitStashCreate extends GitProgressAction<Void> {

	private final IDatabase database;
	private final CategoryDao categoryDao;
	private List<Change> changes;
	private FileRepository git;
	private Commits commits;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;
	private boolean discard;

	private GitStashCreate(IDatabase database) {
		this.database = database;
		this.categoryDao = new CategoryDao(database);
	}

	public static GitStashCreate from(IDatabase database) {
		return new GitStashCreate(database);
	}

	public GitStashCreate changes(List<Change> changes) {
		this.changes = changes;
		return this;
	}

	public GitStashCreate to(FileRepository git) {
		this.git = git;
		this.commits = Commits.of(git);
		return this;
	}

	public GitStashCreate as(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	public GitStashCreate discard() {
		this.discard = true;
		return this;
	}

	public GitStashCreate update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	@Override
	public Void run() throws IOException {
		if (git == null || database == null || workspaceIds == null)
			throw new IllegalStateException("Git repository, database and workspace ids must be set");
		if (!discard && committer == null)
			throw new IllegalStateException("Committer must be set");
		if (progressMonitor != null) {
			progressMonitor.beginTask("Preparing to create stash", -1);
		}
		var config = new GitConfig(database, workspaceIds, git);
		if (changes == null) {
			changes = Diffs.workspace(config).stream().map(Change::new).collect(Collectors.toList());
		}
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		if (!discard) {
			var writer = new CommitWriter(config, committer, progressMonitor);
			writer.stashCommit("Stashed changes", changes);
		}
		runStash();
		for (var category : categoryDao.getRootCategories()) {
			deleteIfAdded(category);
		}
		return null;
	}

	private void runStash() throws IOException {
		var importHelper = new ImportHelper(git, database, workspaceIds, progressMonitor);
		var toDelete = changes.stream()
				.filter(c -> c.diffType == DiffType.ADDED)
				.collect(Collectors.toList());
		var headCommit = commits.head();
		if (headCommit == null) {
			if (progressMonitor != null) {
				progressMonitor.beginTask("Stashing data", toDelete.size());
			}
			importHelper.delete(toDelete);
			workspaceIds.clear();
		} else {
			var toImport = changes.stream()
					.filter(c -> c.diffType != DiffType.ADDED)
					.map(c -> new Reference(c.path, headCommit.id, workspaceIds.getHead(c.path)))
					.collect(Collectors.toList());
			if (progressMonitor != null) {
				progressMonitor.beginTask("Stashing data", toImport.size() + toDelete.size());
			}
			var gitStore = new GitStoreReader(git, headCommit, toImport);
			importHelper.runImport(gitStore);
			importHelper.delete(toDelete);
			var result = new ImportResult(gitStore, toDelete);
			importHelper.updateWorkspaceIds(headCommit.id, result, false);
		}
	}

	private void deleteIfAdded(Category category) {
		var path = workspaceIds.getPath(category);
		if (workspaceIds.get(path).equals(ObjectId.zeroId())) {
			delete(category);
		} else {
			for (var child : category.childCategories) {
				deleteIfAdded(child);
			}
		}
	}

	private void delete(Category category) {
		for (var child : category.childCategories) {
			delete(child);
		}
		categoryDao.delete(category);
	}

}
