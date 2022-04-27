package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.find.Entries;
import org.openlca.git.find.References;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.git.util.GitStoreReader;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

class ImportHelper {

	final References references;
	final Entries entries;
	final IDatabase database;
	final ObjectIdStore workspaceIds;
	ConflictResolver conflictResolver;

	ImportHelper(FileRepository git, IDatabase database, ObjectIdStore workspaceIds) {
		this.references = References.of(git);
		this.entries = Entries.of(git);
		this.database = database;
		this.workspaceIds = workspaceIds;
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

	void runImport(GitStoreReader gitStore) {
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : ImportHelper.TYPE_ORDER) {
			var changes = gitStore.getChanges(type);
			for (var change : changes) {
				jsonImport.run(type, change.refId);
			}
		}
	}

	void delete(List<? extends ModelRef> remoteDeletions) {
		for (var ref : new ArrayList<>(remoteDeletions)) {
			if (conflictResolver != null && conflictResolver.isConflict(ref)) {
				var resolution = conflictResolver.resolveConflict(ref, null);
				if (resolution.type == ConflictResolutionType.KEEP_LOCAL) {
					remoteDeletions.remove(ref);
					continue;
				}
			}
			delete(Daos.root(database, ref.type), ref.refId);
		}
	}

	<T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	void updateWorkspaceIds(Commit remoteCommit, ImportResult result, String mergeCommitId) throws IOException {
		if (workspaceIds == null)
			return;
		result.imported().forEach(ref -> workspaceIds.put(ref.path, ref.objectId));
		result.deleted().forEach(ref -> workspaceIds.remove(ref.path));
		updateCategoryIds(remoteCommit.id, "");
		workspaceIds.putRoot(ObjectId.fromString(remoteCommit.id));
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

	static record ImportResult(List<Reference> imported, List<? extends ModelRef> merged,
			List<? extends ModelRef> keepDeleted, List<? extends ModelRef> deleted) {

		int count() {
			return imported.size() + merged.size() + deleted.size() + keepDeleted.size();
		}

	}
}