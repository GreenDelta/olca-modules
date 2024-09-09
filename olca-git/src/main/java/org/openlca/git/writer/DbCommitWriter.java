package org.openlca.git.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.openlca.core.database.IDatabase;
import org.openlca.git.iterator.ChangeIterator;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.LibraryLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbCommitWriter extends CommitWriter {

	private static final Logger log = LoggerFactory.getLogger(DbCommitWriter.class);
	private String localCommitId;
	private String remoteCommitId;
	private Converter converter;
	private RevCommit parent;
	private ExecutorService threads;
	private IDatabase database;
	private ClientRepository repo;

	public DbCommitWriter(ClientRepository repo) {
		this(repo, new DatabaseBinaryResolver(repo.database));
	}

	public DbCommitWriter(ClientRepository repo, BinaryResolver binaryResolver) {
		super(repo, binaryResolver);
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

	public DbCommitWriter parent(RevCommit parent) {
		this.parent = parent;
		return this;
	}

	public DbCommitWriter merge(String localCommitId, String remoteCommitId) {
		this.localCommitId = localCommitId;
		this.remoteCommitId = remoteCommitId;
		return this;
	}

	public String write(String message, List<Change> changes) throws IOException {
		try {
			progressMonitor.beginTask("Writing data to repository: " + message, changes.size());
			var parentCommitIds = getParentCommitIds();
			usedFeatures = UsedFeatures.of(repo, parentCommitIds);
			var changeIterator = prepare(changes);
			var commitId = write(message, changeIterator, parentCommitIds);
			if (Constants.HEAD.equals(ref)) {
				progressMonitor.beginTask("Updating local index");
				repo.index.reload();
			}
			return commitId;
		} finally {
			cleanUp();
		}
	}

	private ChangeIterator prepare(List<Change> changes) {
		changes = filterInvalid(changes);
		if (changes.isEmpty() && (localCommitId == null || remoteCommitId == null))
			throw new IllegalStateException("No changes found and not a merge commit");
		threads = Executors.newCachedThreadPool();
		converter = new Converter(database, threads, progressMonitor, usedFeatures);
		converter.start(changes.stream()
				.filter(d -> d.changeType != ChangeType.DELETE)
				.sorted()
				.toList());
		return new ChangeIterator(repo, remoteCommitId, binaryResolver, changes);
	}

	private ObjectId[] getParentCommitIds() {
		var parentIds = new ArrayList<ObjectId>();
		if (localCommitId != null) {
			parentIds.add(ObjectId.fromString(localCommitId));
		} else if (parent != null) {
			parentIds.add(parent);
		} else if (repo.getHeadCommit() != null) {
			parentIds.add(repo.getHeadCommit());
		}
		if (remoteCommitId != null) {
			parentIds.add(ObjectId.fromString(remoteCommitId));
		}
		return parentIds.toArray(new ObjectId[parentIds.size()]);
	}

	private List<Change> filterInvalid(List<Change> changes) {
		var remaining = changes.stream()
				.filter(c -> {
					if (c.type == null) {
						var val = "{ path: " + c.path + ", type: " + c.type + ", refId: " + c.refId + "}";
						log.warn("Filtering dataset with missing or invalid type " + val);
						progressMonitor.worked(1);
						return false;						
					}
					if (!GitUtil.isValidCategory(c.category)) {
						var val = "{ path: " + c.path + ", type: " + c.type + ", refId: " + c.refId + "}";
						log.warn("Filtering dataset with invalid category " + val);
						progressMonitor.worked(1);
						return false;
					}
					if (!c.isCategory && !GitUtil.isValidRefId(c.refId)) {
						var val = "{ path: " + c.path + ", type: " + c.type + ", refId: " + c.refId + "}";
						log.warn("Filtering dataset with missing or invalid refId " + val);
						progressMonitor.worked(1);
						return false;
					}
					return true;
				}).collect(Collectors.toList());
		if (remaining.size() != changes.size())

		{
			progressMonitor.worked(changes.size() - remaining.size());
		}
		return remaining;
	}

	protected void cleanUp() throws IOException {
		super.cleanUp();
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

}
