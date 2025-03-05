package org.openlca.git.actions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase.DataPackage;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.library.Unmounter;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.model.Commit;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.ProgressMonitor;

class LibraryMounter {

	private final ClientRepository repo;
	private final Set<DataPackage> localLibs;
	private final Set<DataPackage> remoteLibs;
	private final Set<DataPackage> dbLibs;
	private LibraryResolver libraryResolver;
	private ProgressMonitor progressMonitor;

	private LibraryMounter(ClientRepository repo, Set<DataPackage> localLibs, Set<DataPackage> remoteLibs) {
		this.repo = repo;
		this.localLibs = localLibs;
		this.remoteLibs = remoteLibs;
		this.dbLibs = repo.database.getDataPackages().getAll();
	}

	static LibraryMounter of(ClientRepository repo, Commit localCommit, Commit remoteCommit) {
		return new LibraryMounter(repo, 
				repo.getDataPackages(localCommit), 
				repo.getDataPackages(remoteCommit));
	}

	static LibraryMounter of(ClientRepository repo, Commit remoteCommit) {
		return new LibraryMounter(repo, 
				repo.database.getDataPackages().getAll(),
				repo.getDataPackages(remoteCommit));
	}

	LibraryMounter with(LibraryResolver libraryResolver) {
		this.libraryResolver = libraryResolver;
		return this;
	}

	LibraryMounter with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	MergeResult mountNew() {
		var newLibraries = resolveNewLibraries();
		if (newLibraries.size() == 0)
			return MergeResult.NO_CHANGES;
		progressMonitor.beginTask("Mounting libraries");
		var queue = new ArrayDeque<>(newLibraries);
		var handled = new HashSet<Library>();
		while (!queue.isEmpty()) {
			var next = queue.poll();
			progressMonitor.subTask(next.name());
			if (handled.contains(next))
				continue;
			handled.add(next);
			var checkResult = PreMountCheck.check(repo.database, next);
			if (checkResult.isError())
				return MergeResult.MOUNT_ERROR;
			checkResult.getStates().forEach(p -> handled.add(p.first));
			Mounter.of(repo.database, next)
					.applyDefaultsOf(checkResult)
					.run();
		}
		return MergeResult.SUCCESS;
	}

	private List<Library> resolveNewLibraries() {
		var libs = new ArrayList<Library>();
		for (var remoteLib : remoteLibs) {
			if (dbLibs.contains(remoteLib))
				continue;
			if (libraryResolver == null)
				throw new IllegalStateException("Could not mount libraries because no library resolver was set");
			var lib = libraryResolver.resolve(remoteLib.name());
			if (lib == null)
				return null;
			libs.add(lib);
		}
		return libs;
	}

	void unmountObsolete() {
		var toUnmount = localLibs.stream()
				.filter(Predicate.not(remoteLibs::contains))
				.collect(Collectors.toList());
		for (var lib : toUnmount) {
			if (!dbLibs.contains(lib))
				continue;
			Unmounter.keepNone(repo.database, lib.name());
		}
	}

}
