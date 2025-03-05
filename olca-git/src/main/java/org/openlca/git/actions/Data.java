package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.ProgressMonitor;

class Data {

	private ClientRepository repo;
	private Commit localCommit;
	private Commit remoteCommit;
	private List<Diff> changes;
	private ProgressMonitor progressMonitor;
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

	Data with(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	List<Diff> doImport(Predicate<Diff> doImport, Function<Diff, Reference> toRef) {
		var toImport = changes.stream()
				.filter(diff -> !diff.isDataPackage)
				.filter(doImport)
				.map(toRef)
				.collect(Collectors.toList());
		var gitStore = new GitStoreReader(repo, localCommit, remoteCommit, toImport, conflictResolver);
		return ImportData.from(gitStore)
				.with(progressMonitor)
				.into(repo.database)
				.run();
	}

	List<Diff> doDelete(Predicate<Diff> doDelete, Function<Diff, Reference> toRef) {
		var toDelete = changes.stream()
				.filter(diff -> !diff.isDataPackage)
				.filter(doDelete)
				.map(toRef)
				.collect(Collectors.toList());
		if (toDelete.isEmpty())
			return new ArrayList<>();
		return DeleteData.from(repo.database)
				.with(progressMonitor)
				.with(conflictResolver)
				.data(toDelete)
				.run();
	}

}
