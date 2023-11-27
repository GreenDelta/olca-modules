package org.openlca.git.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.git.model.Change;
import org.openlca.git.model.Commit;
import org.openlca.git.model.DiffType;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.LibraryLink;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbCommitWriter extends CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(DbCommitWriter.class);
	private String localCommitId;
	private String remoteCommitId;
	private Converter converter;
	private Commit reference;
	private ExecutorService threads;
	private IDatabase database;
	private ClientRepository repo;

	public DbCommitWriter(ClientRepository repo) {
		super(repo, new DatabaseBinaryResolver(repo.database));
		this.database = repo.database;
		this.repo = repo;
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

	public DbCommitWriter reference(Commit reference) {
		this.reference = reference;
		return this;
	}

	public DbCommitWriter merge(String localCommitId, String remoteCommitId) {
		this.localCommitId = localCommitId;
		this.remoteCommitId = remoteCommitId;
		return this;
	}

	public String write(String message, List<Change> changes) throws IOException {
		changes = filterInvalid(changes);
		progressMonitor.beginTask("Writing data to repository: " + message, changes.size());
		try {
			var previousCommit = reference == null
					? repo.getHeadCommit()
					: repo.parseCommit(ObjectId.fromString(reference.id));
			if (changes.isEmpty() && (previousCommit == null || localCommitId == null || remoteCommitId == null))
				return null;
			threads = Executors.newCachedThreadPool();
			converter = new Converter(database, threads);
			converter.start(changes.stream()
					.filter(d -> d.diffType != DiffType.DELETED)
					.sorted()
					.toList());
			var commitId = write(message, changes, getParentIds(previousCommit));
			if (Constants.HEAD.equals(ref)) {
				progressMonitor.beginTask("Updating local index");
				repo.index.reload();
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

	private List<Change> filterInvalid(List<Change> changes) {
		var remaining = changes.stream()
				.filter(c -> {
					if (c.isCategory)
						return true;
					if (c.type == null || !GitUtil.isUUID(c.refId)) {
						var val = "{ path: " + c.path + ", type: " + c.type + ", refId: " + c.refId + "}";
						log.warn("Filtering dataset with missing or invalid type or refId " + val);
						progressMonitor.worked(1);
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
		if (remaining.size() != changes.size()) {
			progressMonitor.worked(changes.size() - remaining.size());
		}
		return remaining;
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
	protected List<LibraryLink> getLibraries() {
		return LibraryLink.allOf(database);
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
