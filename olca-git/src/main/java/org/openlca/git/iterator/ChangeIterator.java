package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Change;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.ModelRef;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class ChangeIterator extends EntryIterator {

	private final BinaryResolver binaryResolver;
	private final List<Change> changes;
	private final OlcaRepository repo;
	private final String lastCommitId;

	public ChangeIterator(OlcaRepository repo, BinaryResolver binaryResolver, List<Change> changes) {
		super(initialize(null, changes));
		this.repo = repo;
		this.binaryResolver = binaryResolver;
		this.changes = changes;
		this.lastCommitId = repo.commits.find().latestId();
	}

	private ChangeIterator(ChangeIterator parent, List<Change> changes) {
		super(parent, initialize(parent, changes));
		this.repo = parent.repo;
		this.binaryResolver = parent.binaryResolver;
		this.changes = changes;
		this.lastCommitId = parent.lastCommitId;
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
		this.binaryResolver = parent.binaryResolver;
		this.changes = new ArrayList<>();
		this.lastCommitId = parent.lastCommitId;
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
				if ((change.diffType == DiffType.DELETED && hadBinaries(parent.repo, change, parent.lastCommitId))
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
				&& (parentChange.isEmptyCategory || parentChange.diffType == DiffType.ADDED)) {
			list.add(TreeEntry.empty(parentChange));
			return list;
		}
		if (addEmptyFlag(parent.repo, prefix, changes)) {
			list.add(TreeEntry
					.empty(new Change(DiffType.ADDED, new ModelRef(prefix + "/" + GitUtil.EMPTY_CATEGORY_FLAG))));
		} else if (deleteEmptyFlag(list)) {
			list.add(TreeEntry
					.empty(new Change(DiffType.DELETED, new ModelRef(prefix + "/" + GitUtil.EMPTY_CATEGORY_FLAG))));
		}
		return list;
	}

	private static boolean hadBinaries(OlcaRepository repo, Change change, String lastCommitId) {
		if (Strings.nullOrEmpty(lastCommitId))
			return false;
		var ref = repo.references.get(change.type, change.refId, lastCommitId);
		if (ref == null)
			return false;
		return !repo.references.getBinaries(ref).isEmpty();
	}

	private static boolean addEmptyFlag(OlcaRepository repo, String prefix, List<Change> changes) {
		var entries = repo.entries.find().path(prefix).all();
		if (entries.isEmpty())
			return false;
		var deletions = changes.stream()
				.filter(c -> c.diffType == DiffType.DELETED)
				.map(c -> c.path)
				.collect(Collectors.toSet());
		for (var entry : entries)
			if (!deletions.contains(entry.path))
				return false;
		return true;
	}

	private static boolean deleteEmptyFlag(List<TreeEntry> entries) {
		return entries.stream()
				.filter(e -> {
					if (e.data instanceof Change c && c.diffType == DiffType.ADDED)
						return true;
					return e.data == null && e.fileMode == FileMode.TREE;
				})
				.count() > 0;
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
