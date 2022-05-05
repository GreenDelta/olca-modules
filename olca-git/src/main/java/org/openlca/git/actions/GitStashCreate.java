package org.openlca.git.actions;

import java.io.IOException;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
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
import org.openlca.git.find.References;
import org.openlca.git.model.Change;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.util.GitStoreReader;
import org.openlca.git.writer.CommitWriter;

public class GitStashCreate {

	private final IDatabase database;
	private final CategoryDao categoryDao;
	private FileRepository git;
	private Commits commits;
	private ObjectIdStore workspaceIds;
	private PersonIdent committer;

	private GitStashCreate(IDatabase database) {
		this.database = database;
		this.categoryDao = new CategoryDao(database);
	}

	public static GitStashCreate from(IDatabase database) {
		return new GitStashCreate(database);
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

	public GitStashCreate update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	public void run() throws IOException {
		if (git == null || database == null)
			throw new IllegalStateException("Git repository and database must be set");
		var config = new GitConfig(database, workspaceIds, git);
		var changes = DiffEntries.workspace(config).stream().map(Change::new).toList();
		if (changes.isEmpty())
			throw new IllegalStateException("No changes found");
		var writer = new CommitWriter(config, committer);
		writer.stashCommit("Stashed changes", changes);
		var importHelper = new ImportHelper(git, database, workspaceIds);
		var toDelete = changes.stream()
				.filter(change -> change.changeType == ChangeType.ADD)
				.collect(Collectors.toList());
		var headCommit = commits.head();
		if (headCommit == null) {
			importHelper.delete(toDelete);
			if (workspaceIds != null) {
				workspaceIds.clear();
			}
		} else {
			var references = References.of(git);
			var toImport = changes.stream()
					.filter(change -> change.changeType != ChangeType.ADD)
					.map(change -> references.get(change.type, change.refId, headCommit.id))
					.collect(Collectors.toList());
			var gitStore = new GitStoreReader(git, headCommit, toImport);
			importHelper.runImport(gitStore);
			var result = new ImportResult(gitStore.getImported(), gitStore.getMerged(), gitStore.getKeepDeleted(),
					toDelete);
			importHelper.delete(toDelete);
			importHelper.updateWorkspaceIds(headCommit, result, null);
		}
		for (var category : categoryDao.getRootCategories()) {
			deleteIfAdded(category);
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
