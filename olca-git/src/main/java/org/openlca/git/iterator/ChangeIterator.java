package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.git.model.Change;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class ChangeIterator extends EntryIterator {

	private final BinaryResolver binaryResolver;
	private final List<Change> changes;

	public ChangeIterator(BinaryResolver binaryResolver, List<Change> changes) {
		super(initialize("", binaryResolver, changes));
		this.binaryResolver = binaryResolver;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, List<Change> changes) {
		super(parent, initialize(GitUtil.decode(parent.getEntryPathString()), parent.binaryResolver, changes));
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
		this.binaryResolver = parent.binaryResolver;
		this.changes = new ArrayList<>();
	}

	private static List<TreeEntry> initialize(String prefix, BinaryResolver binaryResolver, List<Change> changes) {
		var list = new ArrayList<TreeEntry>();
		var added = new HashSet<String>();
		changes.forEach(change -> {
			var path = change.path;
			if (!Strings.nullOrEmpty(prefix)) {
				path = path.substring(prefix.length() + 1);
			}
			var name = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
			if (added.contains(name))
				return;
			if (path.contains("/")) {
				list.add(new TreeEntry(name, FileMode.TREE));
			} else {
				list.add(new TreeEntry(name, FileMode.REGULAR_FILE, change));
				if (!binaryResolver.list(change, "").isEmpty()) {
					var bin = name.substring(0, name.indexOf(GitUtil.DATASET_SUFFIX)) + GitUtil.BIN_DIR_SUFFIX;
					list.add(new TreeEntry(bin, FileMode.TREE, change, ""));
				}
			}
			added.add(name);
		});
		return list;
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
