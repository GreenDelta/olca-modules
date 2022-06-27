package org.openlca.git.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.git.util.Repositories;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbCommitWriter extends CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(DbCommitWriter.class);
	private final IDatabase database;
	private ObjectIdStore idStore;
	private String localCommitId;
	private String remoteCommitId;
	private Converter converter;
	private ExecutorService threads;

	public DbCommitWriter(Repository repo, IDatabase database) {
		super(repo, new DatabaseBinaryResolver(database));
		this.database = database;
	}

	@Override
	public DbCommitWriter ref(String ref) {
		super.ref(ref);
		return this;
	}

	@Override
	public DbCommitWriter as(PersonIdent committer) {
		super.as(committer);
		return this;
	}

	@Override
	public DbCommitWriter with(ProgressMonitor progressMonitor) {
		super.with(progressMonitor);
		return this;
	}

	public DbCommitWriter saveIdsIn(ObjectIdStore idStore) {
		this.idStore = idStore;
		return this;
	}

	public DbCommitWriter merge(String localCommitId, String remoteCommitId) {
		this.localCommitId = localCommitId;
		this.remoteCommitId = remoteCommitId;
		return this;
	}

	public String write(String message, List<Change> changes) throws IOException {
		try {
			var previousCommit = Repositories.headCommitOf(repo);
			if (changes.isEmpty() && (previousCommit == null || localCommitId == null || remoteCommitId == null))
				return null;
			threads = Executors.newCachedThreadPool();
			converter = new Converter(database, threads);
			converter.start(changes.stream()
					.filter(d -> d.diffType != DiffType.DELETED)
					.sorted()
					.toList());
			var commitId = write(message, changes, getParentIds(previousCommit));
			if (idStore != null) {
				idStore.save();
			}
			return commitId;
		} finally {
			close();
		}
	}

	private ObjectId[] getParentIds(RevCommit previousCommit) {
		var parentIds = new ArrayList<ObjectId>();
		if (localCommitId != null) {
			parentIds.add(ObjectId.fromString(localCommitId));
		} else if (previousCommit != null) {
			parentIds.add(previousCommit.getId());
		}
		if (remoteCommitId != null) {
			parentIds.add(ObjectId.fromString(remoteCommitId));
		}
		return parentIds.toArray(new ObjectId[parentIds.size()]);
	}

	protected void close() throws IOException {
		super.close();
		if (converter != null) {
			converter.clear();
			converter = null;
		}
		if (threads != null) {
			threads.shutdown();
		}
	}

	@Override
	protected void inserted(String path, ObjectId id) {
		if (idStore != null) {
			idStore.put(path, id);
		}
	}

	@Override
	protected void removed(String path) {
		if (idStore != null) {
			idStore.remove(path);
		}
	}

	@Override
	protected Set<String> getLibraries() {
		return database.getLibraries();
	}

	@Override
	protected byte[] getData(Change change) {
		try {
			return converter.take(change.path);
		} catch (InterruptedException e) {
			log.error("Error taking data for " + change.path, e);
			return null;
		}
	}

	private static class DatabaseBinaryResolver implements BinaryResolver {

		private final FileStore fileStore;

		private DatabaseBinaryResolver(IDatabase database) {
			this.fileStore = new FileStore(database);
		}

		@Override
		public List<String> list(Change change, String relativePath) {
			var root = getFile(change, null).toPath();
			var files = getFile(change, relativePath).listFiles();
			if (files == null)
				return new ArrayList<>();
			return Arrays.asList(files).stream()
					.map(File::toPath)
					.map(root::relativize)
					.map(Path::toString)
					.toList();
		}

		@Override
		public boolean isDirectory(Change change, String relativePath) {
			return getFile(change, relativePath).isDirectory();
		}

		@Override
		public byte[] resolve(Change change, String relativePath) throws IOException {
			return Files.readAllBytes(getFile(change, relativePath).toPath());
		}

		private File getFile(Change change, String relativePath) {
			var folder = fileStore.getFolder(change.type, change.refId);
			if (!Strings.nullOrEmpty(relativePath)) {
				folder = new File(folder, relativePath);
			}
			return folder;
		}

	}

}
