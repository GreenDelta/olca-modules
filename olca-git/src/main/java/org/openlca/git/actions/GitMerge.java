package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.git.GitConfig;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.find.Commits;
import org.openlca.git.find.Entries;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.Reference;
import org.openlca.git.util.Constants;
import org.openlca.git.util.DiffEntries;
import org.openlca.git.util.JsonGitReader;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

public class GitMerge {

	private static final ModelType[] typeOrder = new ModelType[] {
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.PARAMETER,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.CURRENCY,
			ModelType.FLOW,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT
	};
	private final FileRepository git;
	private IDatabase database;
	private ObjectIdStore workspaceIds;

	private GitMerge(FileRepository git) {
		this.git = git;
	}

	public static GitMerge from(FileRepository git) {
		return new GitMerge(git);
	}

	public GitMerge into(IDatabase database) {
		this.database = database;
		return this;
	}

	public GitMerge update(ObjectIdStore workspaceIds) {
		this.workspaceIds = workspaceIds;
		return this;
	}

	public List<Reference> run() throws IOException {
		var commits = Commits.of(git);
		var localCommitId = commits.resolve(Constants.LOCAL_BRANCH);
		var remoteCommitId = commits.resolve(Constants.REMOTE_BRANCH);
		var behind = commits.find()
				.after(localCommitId)
				.until(remoteCommitId)
				.all();
		if (behind.isEmpty())
			return new ArrayList<>();
		var commit = commits.get(localCommitId);
		var changed = new HashSet<String>();
		if (workspaceIds != null) {
			var config = new GitConfig(database, workspaceIds, git, null);
			changed.addAll(DiffEntries.workspace(config, commit).stream()
					.map(d -> d.getChangeType() == ChangeType.DELETE ? d.getOldPath() : d.getNewPath())
					.collect(Collectors.toSet()));
		}
		var imported = runImport(localCommitId, remoteCommitId);
		if (workspaceIds != null) {
			changed.forEach(path -> workspaceIds.invalidate(path));
			workspaceIds.save();
		}
		return imported;
	}

	private List<Reference> runImport(String localCommitId, String remoteCommitId) throws IOException {
		var imported = new ArrayList<Reference>();
		var jsonStore = new JsonGitReader(git, localCommitId, remoteCommitId);
		var jsonImport = new JsonImport(jsonStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : typeOrder) {
			var changes = jsonStore.getChanges(type);
			for (var change : changes) {
				jsonImport.run(type, change.refId);
				imported.add(change);
				if (workspaceIds != null) {
					workspaceIds.put(change.fullPath, change.objectId);
				}
			}
		}
		if (workspaceIds != null) {
			updateCategoryIds(remoteCommitId, "");
			workspaceIds.putRoot(ObjectId.fromString(remoteCommitId));
		}
		updateHeadRef(remoteCommitId);
		return imported;
	}

	private void updateCategoryIds(String remoteCommitId, String path) {
		Entries.of(git).find().commit(remoteCommitId).path(path).all().forEach(entry -> {
			if (entry.typeOfEntry == EntryType.DATASET)
				return;
			workspaceIds.put(entry.fullPath, entry.objectId);
			updateCategoryIds(remoteCommitId, entry.fullPath);
		});
	}

	private void updateHeadRef(String remoteCommitId) throws IOException {
		var update = git.updateRef(Constants.LOCAL_BRANCH);
		update.setNewObjectId(ObjectId.fromString(remoteCommitId));
		update.update();
	}

}
