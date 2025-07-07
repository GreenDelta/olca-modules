package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openlca.core.database.DataPackage;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ConflictResolver.GitContext;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.actions.GitMerge.MergeResultType;
import org.openlca.git.actions.GitStoreReader.MergedDataImpl;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.ProgressMonitor;

class Data {

	private ClientRepository repo;
	private Commit localCommit;
	private Commit remoteCommit;
	private List<Diff> changes;
	private ProgressMonitor progressMonitor;
	private DataPackage dataPackage;
	private DependencyResolver dependencyResolver;
	private ConflictResolver conflictResolver;

	static Data of(ClientRepository repo, Commit remoteCommit) {
		return of(repo, null, remoteCommit);
	}

	static Data of(ClientRepository repo, Commit localCommit, Commit remoteCommit) {
		var data = new Data();
		data.repo = repo;
		data.localCommit = localCommit;
		data.remoteCommit = remoteCommit;
		return data;
	}

	Data changes(List<Diff> changes) {
		this.changes = changes;
		return this;
	}

	Data with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	Data with(DependencyResolver libraryResolver) {
		this.dependencyResolver = libraryResolver;
		return this;
	}

	Data with(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	Data into(DataPackage dataPackage) {
		this.dataPackage = dataPackage;
		return this;
	}

	UpdateResult update() throws IOException, GitAPIException {
		var dataPackages = DataPackageMounter.of(repo, localCommit, remoteCommit)
				.with(dependencyResolver)
				.with(conflictResolver)
				.with(progressMonitor);
		var mountResult = dataPackages.mountNew();
		if (mountResult.type() == MergeResultType.ABORTED || mountResult.type() == MergeResultType.MOUNT_ERROR)
			return new UpdateResult(mountResult);
		var changes = separateChanges();
		var mergedData = doImport(changes.toImport);
		doDelete(changes.toDelete).forEach(mergedData::delete);
		dataPackages.unmountObsolete();
		progressMonitor.beginTask("Reloading descriptors");
		repo.descriptors.reload();
		return new UpdateResult(mountResult, mergedData);
	}

	private Changes separateChanges() {
		var changes = new Changes();
		for (var change : this.changes) {
			if (change.isDataPackage)
				continue;
			if (doDelete(change)) {
				changes.toDelete.add(change.oldRef);
			} else {
				// in case a deleted data set is kept in local, but
				// merged/overwritten in workspace, see doDelete(Diff)
				if (change.diffType == DiffType.DELETED) {
					changes.toImport.add(new Reference(change.path));
				} else {
					changes.toImport.add(change.newRef);
				}
			}
		}
		return changes;
	}

	private boolean doDelete(Diff change) {
		if (change.diffType != DiffType.DELETED)
			return false;
		if (!conflictResolver.isConflict(change))
			return true;
		// if the data set was deleted in remote, but kept in local and then
		// overwritten or merged in workspace, the workspace version needs to be
		// updated, so this needs to be in "toImport" instead of "toDelete"
		// other conflict resolution is correctly applied within GitStoreReader
		// and DeleteData classes, but this specific case must be handled here
		var resolution = conflictResolver.peekConflictResolution(change);
		if (resolution == null || resolution.context != GitContext.LOCAL
				|| resolution.type != ConflictResolutionType.KEEP)
			return true;
		var workspaceResolution = conflictResolver.peekConflictResolutionWithWorkspace(change);
		if (workspaceResolution == null)
			return true;
		return workspaceResolution.type != ConflictResolutionType.OVERWRITE
				&& workspaceResolution.type != ConflictResolutionType.MERGE;
	}

	private MergedDataImpl doImport(List<Reference> toImport) {
		if (remoteCommit == null || toImport.isEmpty())
			return new MergedDataImpl(repo, localCommit);
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, toImport)
				.into(dataPackage)
				.resolveConflictsWith(conflictResolver);
		return ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
	}

	private List<Diff> doDelete(List<Reference> toDelete) {
		if (toDelete.isEmpty())
			return new ArrayList<>();
		return DeleteData.from(repo.database)
				.with(progressMonitor)
				.with(conflictResolver)
				.data(toDelete)
				.run();
	}

	private record Changes(List<Reference> toImport, List<Reference> toDelete) {
		private Changes() {
			this(new ArrayList<>(), new ArrayList<>());
		}
	}

	record UpdateResult(MergeResult mergeResult, MergedData mergedData) {

		private UpdateResult(MergeResult mergeResult) {
			this(mergeResult, null);
		}

	}

}
