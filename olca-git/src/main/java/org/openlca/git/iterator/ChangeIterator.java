package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.model.Commit;
import org.openlca.git.model.ModelRef;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class ChangeIterator extends EntryIterator {

	private final OlcaRepository repo;
	private final Commit referenceCommit;
	private final BinaryResolver binaryResolver;
	private final List<Change> changes;

	public ChangeIterator(OlcaRepository repo, BinaryResolver binaryResolver, List<Change> changes) {
		this(repo, null, binaryResolver, changes);
	}

	public ChangeIterator(OlcaRepository repo, String referenceCommitId, BinaryResolver binaryResolver,
			List<Change> changes) {
		super(initialize(null, changes));
		this.repo = repo;
		this.referenceCommit = referenceCommitId != null
				? repo.commits.get(referenceCommitId)
				: repo.commits.find().latest();
		this.binaryResolver = binaryResolver;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, List<Change> changes) {
		super(parent, initialize(parent, changes));
		this.repo = parent.repo;
		this.referenceCommit = parent.referenceCommit;
		this.binaryResolver = parent.binaryResolver;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, Change change, String filePath) {
		super(parent, parent.binaryResolver.list(change, filePath).stream()
				.map(path -> {
					var name = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
					if (parent.binaryResolver.isDirectory(change, path))
						return new TreeEntry(name, FileMode.TREE, change, path);
					return new TreeEntry(name, FileMode.REGULAR_FILE, change, path);
				})
				.toList());
		this.repo = parent.repo;
		this.referenceCommit = parent.referenceCommit;
		this.binaryResolver = parent.binaryResolver;
		this.changes = new ArrayList<>();
	}

	private static List<TreeEntry> initialize(ChangeIterator parent, List<Change> changes) {
		var list = new ArrayList<TreeEntry>();
		var added = new HashSet<String>();
		var prefix = parent != null
				? GitUtil.decode(parent.getEntryPathString())
				: "";
		changes.stream().sorted().forEach(change -> {
			var path = !Strings.nullOrEmpty(prefix)
					? change.path.substring(prefix.length() + 1)
					: change.path;
			var name = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
			if (added.contains(name))
				return;
			if (path.contains("/")) {
				list.add(new TreeEntry(name, FileMode.TREE));
			} else if (change.isCategory) {
				list.add(new TreeEntry(name, FileMode.TREE, change));
			} else {
				list.add(new TreeEntry(name, FileMode.REGULAR_FILE, change));
				if ((change.changeType == ChangeType.DELETE && hadBinaries(parent.repo, change, parent.referenceCommit))
						|| !parent.binaryResolver.list(change, "").isEmpty()) {
					var bin = name.substring(0, name.indexOf(GitUtil.DATASET_SUFFIX)) + GitUtil.BIN_DIR_SUFFIX;
					list.add(new TreeEntry(bin, FileMode.TREE, change, ""));
				}
			}
			added.add(name);
		});
		if (parent == null) {
			list.add(new TreeEntry(RepositoryInfo.FILE_NAME, FileMode.REGULAR_FILE));
		}
		if (!prefix.contains("/"))
			return list;
		var parentChange = parent != null ? parent.getEntryData() : null;
		if (list.isEmpty() && parentChange != null
				&& (parentChange.isEmptyCategory || parentChange.changeType == ChangeType.ADD)) {
			list.add(TreeEntry.empty(parentChange));
			return list;
		}
		if (allExistingEntriesWillBeDeleted(parent.repo, parent.referenceCommit, prefix, changes)) {
			list.add(TreeEntry.empty(Change.add(new ModelRef(prefix + "/" + GitUtil.EMPTY_CATEGORY_FLAG))));
		} else if (datasetWasAddedToEmptyCategory(parent.repo, parent.referenceCommit, prefix, list)) {
			list.add(TreeEntry.empty(Change.delete(new ModelRef(prefix + "/" + GitUtil.EMPTY_CATEGORY_FLAG))));
		}
		return list;
	}

	private static boolean hadBinaries(OlcaRepository repo, Change change, Commit referenceCommit) {
		if (referenceCommit == null)
			return false;
		var ref = repo.references.get(change.type, change.refId, referenceCommit.id);
		if (ref == null)
			return false;
		return !repo.references.getBinaries(ref).isEmpty();
	}

	private static boolean allExistingEntriesWillBeDeleted(OlcaRepository repo, Commit referenceCommit, String prefix,
			List<Change> changes) {
		if (referenceCommit == null)
			return false; // no existing entries
		var entries = repo.entries.find().commit(referenceCommit.id).path(prefix).all();
		if (entries.isEmpty())
			return false; // no existing entries
		var deletions = changes.stream()
				.filter(c -> c.changeType == ChangeType.DELETE)
				.map(c -> c.path)
				.collect(Collectors.toSet());
		for (var entry : entries)
			if (!deletions.contains(entry.path))
				return false; // at least one entry remains
		return true;
	}

	private static boolean datasetWasAddedToEmptyCategory(OlcaRepository repo, Commit referenceCommit, String prefix,
			List<TreeEntry> entries) {
		if (referenceCommit == null)
			return false;
		var newData = entries.stream()
				.filter(e -> {
					if (e.data instanceof Change c && c.changeType == ChangeType.ADD)
						return true;
					return e.data == null && e.fileMode == FileMode.TREE;
				})
				.count() > 0;
		if (!newData)
			return false;
		var first = prefix.substring(0, prefix.lastIndexOf("/"));
		var last = prefix.substring(prefix.lastIndexOf("/") + 1);
		return repo.entries.find().commit(referenceCommit.id).path(first).contains(last)
				&& repo.entries.find().commit(referenceCommit.id).path(prefix).count() == 0;
	}

	public final ChangeIterator createSubtreeIterator() {
		return createSubtreeIterator(null);
	}

	@Override
	public ChangeIterator createSubtreeIterator(ObjectReader reader) {
		var data = getEntryData();
		var filePath = getEntryFilePath();
		if (data != null && filePath != null)
			return new ChangeIterator(this, data, filePath);
		var path = GitUtil.decode(getEntryPathString());
		return new ChangeIterator(this, changes.stream()
				.filter(d -> d.path.startsWith(path + "/"))
				.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Change getEntryData() {
		return super.getEntryData();
	}

}
