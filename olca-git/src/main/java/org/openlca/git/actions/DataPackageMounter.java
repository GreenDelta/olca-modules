package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.openlca.core.database.IDatabase.DataPackage;
import org.openlca.core.library.Library;
import org.openlca.core.library.Mounter;
import org.openlca.core.library.PreMountCheck;
import org.openlca.core.library.Unmounter;
import org.openlca.git.actions.DependencyResolver.IResolvedDependency;
import org.openlca.git.actions.GitMerge.MergeResult;
import org.openlca.git.actions.GitMerge.MergeResultType;
import org.openlca.git.model.Commit;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.ProgressMonitor;

class DataPackageMounter {

	private final ClientRepository repo;
	private final Set<DataPackage> localPackages;
	private final Set<DataPackage> remotePackages;
	private final Set<DataPackage> dbPackages;
	private DependencyResolver dependencyResolver;
	private ConflictResolver conflictResolver;
	private ProgressMonitor progressMonitor;

	private DataPackageMounter(ClientRepository repo, Set<DataPackage> localLibs, Set<DataPackage> remoteLibs) {
		this.repo = repo;
		this.localPackages = localLibs;
		this.remotePackages = remoteLibs;
		this.dbPackages = repo.database.getDataPackages().getAll();
	}

	static DataPackageMounter of(ClientRepository repo, Commit localCommit, Commit remoteCommit) {
		return new DataPackageMounter(repo,
				repo.getDataPackages(localCommit),
				repo.getDataPackages(remoteCommit));
	}

	static DataPackageMounter of(ClientRepository repo, Commit remoteCommit) {
		return new DataPackageMounter(repo,
				repo.database.getDataPackages().getAll(),
				repo.getDataPackages(remoteCommit));
	}

	DataPackageMounter with(DependencyResolver libraryResolver) {
		this.dependencyResolver = libraryResolver;
		return this;
	}

	DataPackageMounter with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	DataPackageMounter with(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver;
		return this;
	}

	MergeResult mountNew() throws IOException, GitAPIException {
		var newPackages = resolveNew();
		if (newPackages.size() == 0)
			return new MergeResult(MergeResultType.NO_CHANGES);
		progressMonitor.beginTask("Mounting data packages");
		var queue = new ArrayDeque<>(newPackages);
		var handled = new HashSet<DataPackage>();
		while (!queue.isEmpty()) {
			var next = queue.poll();
			var name = next.dataPackage().name();
			var task = next.dataPackage().isLibrary()
					? "Library " + name
					: "Data package " + name;
			progressMonitor.subTask(task);
			if (handled.contains(next.dataPackage()))
				continue;
			handled.add(next.dataPackage());
			try {
				if (next.dependency() instanceof Library lib) {
					handled.addAll(mountLibrary(lib));
				} else if (next.dependency() instanceof ClientRepository repo) {
					var mergeResult = mountDataPackage(next.dataPackage(), repo);
					handled.addAll(mergeResult.mountedDataPackages());
					if (mergeResult.type() == MergeResultType.ABORTED)
						return new MergeResult(MergeResultType.ABORTED, handled);
					if (mergeResult.type() == MergeResultType.MOUNT_ERROR)
						return new MergeResult(MergeResultType.MOUNT_ERROR, handled);
				}
			} catch (MountException e) {
				return new MergeResult(MergeResultType.MOUNT_ERROR, handled);
			}
		}
		return new MergeResult(MergeResultType.SUCCESS, handled);
	}

	private Set<DataPackage> mountLibrary(Library library) throws MountException {
		var handled = new HashSet<DataPackage>();
		var checkResult = PreMountCheck.check(repo.database, library);
		if (checkResult.isError())
			throw new MountException();
		checkResult.getStates()
				.forEach(p -> handled.add(
						DataPackage.library(p.first.name(), null)));
		Mounter.of(repo.database, library)
				.applyDefaultsOf(checkResult)
				.run();
		return handled;
	}

	private MergeResult mountDataPackage(DataPackage dataPackage, ClientRepository repo)
			throws IOException, GitAPIException {
		var commit = repo.commits.get(dataPackage.version());
		if (commit == null)
			throw new IllegalStateException(
					"Could not find commit " + dataPackage.version() + " in repository " + dataPackage.name());
		var result = GitMerge.on(repo)
				.commit(commit)
				.into(dataPackage)
				.resolveConflictsWith(conflictResolver)
				.resolveDependenciesWith(dependencyResolver)
				.withProgress(progressMonitor)
				.run();
		if (result.type() == MergeResultType.MOUNT_ERROR || result.type() == MergeResultType.ABORTED)
			return result;
		repo.database.addDataPackage(dataPackage.name(), dataPackage.version(), dataPackage.url());
		return result;
	}

	private List<IResolvedDependency<?>> resolveNew() {
		var dependencies = new ArrayList<IResolvedDependency<?>>();
		for (var remote : remotePackages) {
			if (dbPackages.contains(remote))
				continue;
			if (dependencyResolver == null)
				throw new IllegalStateException("Could not mount data packages because no dependency resolver was set");
			var resolved = dependencyResolver.resolve(remote);
			if (resolved == null || resolved.dependency() == null)
				return null;
			dependencies.add(resolved);
		}
		return dependencies;
	}

	void unmountObsolete() {
		var toUnmount = localPackages.stream()
				.filter(Predicate.not(remotePackages::contains))
				.collect(Collectors.toList());
		for (var next : toUnmount) {
			if (!dbPackages.contains(next))
				continue;
			Unmounter.keepNone(repo.database, next.name());
		}
	}

	class MountException extends IOException {

		private static final long serialVersionUID = 7656323523482743101L;

	}

}
