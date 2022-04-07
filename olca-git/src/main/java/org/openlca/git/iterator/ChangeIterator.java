package org.openlca.git.iterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.core.database.FileStore;
import org.openlca.git.GitConfig;
import org.openlca.git.model.Change;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class ChangeIterator extends EntryIterator {

	private final GitConfig config;
	private final List<Change> changes;

	public ChangeIterator(GitConfig config, List<Change> changes) {
		super(initialize(null, config, changes));
		this.config = config;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, List<Change> changes) {
		super(parent, initialize(parent.getEntryPathString(), parent.config, changes));
		this.config = parent.config;
		this.changes = changes;
	}

	private ChangeIterator(ChangeIterator parent, Change change, File binDir) {
		super(parent, Arrays.asList(binDir.listFiles()).stream()
				.map(file -> {
					var mode = file.isDirectory() ? FileMode.TREE : FileMode.REGULAR_FILE;
					return new TreeEntry(file.getName(), mode, change, file);
				})
				.toList());
		this.config = parent.config;
		this.changes = new ArrayList<>();
	}

	private static List<TreeEntry> initialize(String prefix, GitConfig config, List<Change> changes) {
		var list = new ArrayList<TreeEntry>();
		var added = new HashSet<String>();
		changes.forEach(d -> {
			var path = d.path;
			if (!Strings.nullOrEmpty(prefix)) {
				path = path.substring(prefix.length() + 1);
			}
			var name = path.contains("/") ? path.substring(0, path.indexOf('/')) : path;
			if (added.contains(name))
				return;
			if (path.contains("/")) {
				list.add(new TreeEntry(name, FileMode.TREE));
			} else {
				list.add(new TreeEntry(name, FileMode.REGULAR_FILE, d));
				var binaryDir = getBinaryDir(config, d);
				if (binaryDir != null) {
					var bin = name.substring(0, name.indexOf(GitUtil.DATASET_SUFFIX)) + GitUtil.BIN_DIR_SUFFIX;
					list.add(new TreeEntry(bin, FileMode.TREE, d, binaryDir));
				}
			}
			added.add(name);
		});
		return list;
	}

	private static File getBinaryDir(GitConfig config, Change change) {
		var filestore = new FileStore(config.database);
		var folder = filestore.getFolder(change.type, change.refId);
		if (!folder.exists() || folder.listFiles().length == 0)
			return null;
		return folder;
	}

	public ChangeIterator createSubtreeIterator() {
		return createSubtreeIterator(null);
	}

	@Override
	public ChangeIterator createSubtreeIterator(ObjectReader reader) {
		var entry = getEntry();
		if (entry.data instanceof Change change && entry.file != null)
			return new ChangeIterator(this, change, entry.file);
		var path = getEntryPathString();
		return new ChangeIterator(this, changes.stream()
				.filter(d -> d.path.startsWith(path + "/"))
				.toList());
	}

}
