package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.commons.Strings;

public class ChangeIterator extends EntryIterator {

	private final OlcaRepository repo;
	private final Commit referenceCommit;
	private final BinaryResolver binaryResolver;
	private final List<Diff> changes;
	private final boolean keepEmptyCategories;

	public static ChangeIterator discardEmptiedCategories(OlcaRepository repo, BinaryResolver binaryResolver, List<Diff> diffs) {
		return new ChangeIterator(repo, null, binaryResolver, splitMoved(repo, diffs), false);
	}

	public static ChangeIterator of(OlcaRepository repo, String referenceCommitId,
			BinaryResolver binaryResolver, List<Diff> diffs) {
		return new ChangeIterator(repo, referenceCommitId, binaryResolver, splitMoved(repo, diffs), true);
	}

	private ChangeIterator(OlcaRepository repo, String referenceCommitId, BinaryResolver binaryResolver,
			List<Diff> changes, boolean keepEmptyCategories) {
		super(initialize(null, changes, keepEmptyCategories));
		this.repo = repo;
		this.keepEmptyCategories = keepEmptyCategories;
		this.referenceCommit = referenceCommitId != null
				? repo.commits.get(referenceCommitId)
				: repo.commits.find().latest();
		this.binaryResolver = binaryResolver;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, List<Diff> changes, boolean keepEmptyCategories) {
		super(parent, initialize(parent, changes, keepEmptyCategories));
		this.repo = parent.repo;
		this.keepEmptyCategories = keepEmptyCategories;
		this.referenceCommit = parent.referenceCommit;
		this.binaryResolver = parent.binaryResolver;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, Diff change, String filePath) {
		super(parent, parent.binaryResolver.list(change, filePath).stream()
				.map(path -> {
					var name = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
					if (parent.binaryResolver.isDirectory(change, path))
						return new TreeEntry(name, FileMode.TREE, change, path);
					return new TreeEntry(name, FileMode.REGULAR_FILE, change, path);
				})
				.toList());
		this.repo = parent.repo;
		this.keepEmptyCategories = true;
		this.referenceCommit = parent.referenceCommit;
		this.binaryResolver = parent.binaryResolver;
		this.changes = new ArrayList<>();
	}

	private static List<Diff> splitMoved(OlcaRepository repo, List<Diff> changes) {
		var split = new ArrayList<Diff>();
		for (var change : changes) {
			if (change.diffType == DiffType.MOVED) {
				split.add(Diff.added(change.newRef));
				split.add(Diff.deleted(change.oldRef));
			} else if (change.isLibrary) {
				if (repo.commits.head() == null) {
					split.add(Diff.added(new Reference(RepositoryInfo.FILE_NAME)));
				} else {
					split.add(Diff.modified(
							new Reference(RepositoryInfo.FILE_NAME),
							new Reference(RepositoryInfo.FILE_NAME)));
				}
			} else {
				split.add(change);
			}
		}
		return split;
	}

	private static List<TreeEntry> initialize(ChangeIterator parent, List<Diff> changes, boolean keepEmptyCategories) {
		var list = new ArrayList<TreeEntry>();
		var added = new HashSet<String>();
		var prefix = parent != null
				? GitUtil.decode(parent.getEntryPathString())
				: "";
		changes.stream().sorted().forEach(change -> {
			var path = Strings.isNotBlank(prefix)
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
				if (hasBinaries(parent, change)) {
					var refId = GitUtil.getRefId(name);
					var bin = GitUtil.toBinDirName(refId);
					list.add(new TreeEntry(bin, FileMode.TREE, change, ""));
				}
			}
			added.add(name);
		});
		if (parent == null && changes.stream().filter(c -> c.isRepositoryInfo).count() == 0) {
			list.add(new TreeEntry(RepositoryInfo.FILE_NAME, FileMode.REGULAR_FILE));
		}
		if (!prefix.contains("/"))
			return list;
		var parentChange = parent != null ? parent.getEntryData() : null;
		if (list.isEmpty() && parentChange != null
				&& (parentChange.isEmptyCategory || parentChange.diffType == DiffType.ADDED)) {
			list.add(TreeEntry.empty(parentChange));
			return list;
		}
		if (keepEmptyCategories && allExistingEntriesWillBeDeleted(parent.repo, parent.referenceCommit, prefix, changes)) {
			list.add(TreeEntry.empty(Diff.added(new Reference(GitUtil.toEmptyCategoryPath(prefix)))));
		} else if (datasetWasAddedToEmptyCategory(parent.repo, parent.referenceCommit, prefix, list)) {
			list.add(TreeEntry.empty(Diff.deleted(new Reference(GitUtil.toEmptyCategoryPath(prefix)))));
		}
		return list;
	}

	private static boolean hasBinaries(ChangeIterator parent, Diff change) {
		if (change.isRepositoryInfo || parent == null)
			return false;
		if (change.diffType == DiffType.DELETED && !hadBinaries(parent.repo, change, parent.referenceCommit))
			return false;
		return !parent.binaryResolver.list(change, "").isEmpty();
	}

	private static boolean hadBinaries(OlcaRepository repo, Diff change, Commit referenceCommit) {
		if (referenceCommit == null)
			return false;
		var ref = repo.references.get(change.type, change.refId, referenceCommit.id);
		if (ref == null)
			return false;
		return !repo.references.getBinaries(ref).isEmpty();
	}

	private static boolean allExistingEntriesWillBeDeleted(OlcaRepository repo, Commit referenceCommit, String prefix,
			List<Diff> changes) {
		if (referenceCommit == null)
			return false; // no existing entries
		var paths = new HashSet<String>();
		repo.references.find().includeCategories().commit(referenceCommit.id).path(prefix)
				.iterate(ref -> paths.add(ref.path));
		if (paths.isEmpty())
			return false; // no existing entries
		var deletions = changes.stream()
				.filter(d -> d.diffType == DiffType.DELETED)
				.map(d -> d.path)
				.collect(Collectors.toSet());
		for (var path : paths)
			if (!deletions.contains(path))
				return false; // at least one entry remains
		return changes.stream().filter(d -> d.diffType == DiffType.ADDED)
				.map(d -> d.path)
				.count() == 0; // no new entries added
	}

	private static boolean datasetWasAddedToEmptyCategory(OlcaRepository repo, Commit referenceCommit, String prefix,
			List<TreeEntry> entries) {
		if (referenceCommit == null)
			return false;
		var newData = entries.stream()
				.filter(e -> {
					if (e.data instanceof Diff d && d.diffType == DiffType.ADDED)
						return true;
					return e.data == null && e.fileMode == FileMode.TREE;
				})
				.count() > 0;
		if (!newData)
			return false;
		var first = prefix.substring(0, prefix.lastIndexOf("/"));
		var last = prefix.substring(prefix.lastIndexOf("/") + 1);
		return repo.references.find().includeCategories().commit(referenceCommit.id).path(first).contains(last)
				&& repo.references.find().includeCategories().commit(referenceCommit.id).path(prefix).count() == 0;
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
				.collect(Collectors.toList()), keepEmptyCategories);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Diff getEntryData() {
		return super.getEntryData();
	}

}
