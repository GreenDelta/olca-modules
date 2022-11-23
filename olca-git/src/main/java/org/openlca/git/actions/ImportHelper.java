package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ImportResults.ImportState;
import org.openlca.git.find.Entries;
import org.openlca.git.find.References;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

class ImportHelper {

	final References references;
	final Entries entries;
	final IDatabase database;
	final ObjectIdStore workspaceIds;
	final ProgressMonitor progressMonitor;
	ConflictResolver conflictResolver = ConflictResolver.NULL;

	ImportHelper(Repository repo, IDatabase database, ObjectIdStore workspaceIds, ProgressMonitor progressMonitor) {
		this.references = References.of(repo);
		this.entries = Entries.of(repo);
		this.database = database;
		this.workspaceIds = workspaceIds;
		this.progressMonitor = progressMonitor;
	}

	static final ModelType[] TYPE_ORDER = new ModelType[] {
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
			ModelType.RESULT,
			ModelType.EPD
	};

	private String getLabel(ModelType type) {
		if (type == ModelType.PROCESS)
			return "processes";
		if (type == ModelType.IMPACT_CATEGORY)
			return "impact categories";
		if (type == ModelType.FLOW_PROPERTY)
			return "flow properties";
		return type.name().toLowerCase().replace("_", " ") + "s";
	}

	void runImport(GitStoreReader gitStore) {
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : ImportHelper.TYPE_ORDER) {
			var changes = gitStore.getChanges(type);
			if (changes.isEmpty())
				continue;
			progressMonitor.subTask("Importing " + getLabel(type));
			for (var change : changes) {
				if (change != null) {
					jsonImport.run(type, change.refId);
				}
				progressMonitor.worked(1);
			}
		}
	}

	void delete(List<? extends ModelRef> remoteDeletions) {
		for (var ref : new ArrayList<>(remoteDeletions)) {
			progressMonitor.subTask("Deleting", ref);
			if (keepLocal(ref)) {
				remoteDeletions.remove(ref);
			} else {
				delete(Daos.root(database, ref.type), ref.refId);
			}
			progressMonitor.worked(1);
		}
	}

	private boolean keepLocal(ModelRef ref) {
		if (!conflictResolver.isConflict(ref))
			return false;
		var resolution = conflictResolver.resolveConflict(ref, null);
		return resolution.type == ConflictResolutionType.KEEP;
	}

	private <T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	void updateWorkspaceIds(String commitId, ImportResults result, boolean applyStash) throws IOException {
		if (workspaceIds == null)
			return;
		result.get(ImportState.UPDATED).forEach(ref -> {
			if (applyStash) {
				workspaceIds.invalidate(ref.path);
			} else {
				workspaceIds.put(ref.path, ref.objectId);
			}
		});
		result.get(ImportState.DELETED).forEach(ref -> workspaceIds.remove(ref.path));
		updateCategoryIds(commitId, "");
		workspaceIds.putRoot(ObjectId.fromString(commitId));
		workspaceIds.save();
	}

	private void updateCategoryIds(String remoteCommitId, String path) {
		entries.find().commit(remoteCommitId).path(path).all().forEach(entry -> {
			if (entry.typeOfEntry == EntryType.DATASET)
				return;
			workspaceIds.put(entry.path, entry.objectId);
			updateCategoryIds(remoteCommitId, entry.path);
		});
	}
}
