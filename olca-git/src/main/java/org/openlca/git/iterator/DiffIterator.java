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
import org.openlca.git.model.Diff;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class DiffIterator extends EntryIterator {

	private final GitConfig config;
	private final List<Diff> diffs;

	public DiffIterator(GitConfig config, List<Diff> diffs) {
		super(initialize(null, config, diffs));
		this.config = config;
		this.diffs = diffs;
	}

	private DiffIterator(DiffIterator parent, List<Diff> diffs) {
		super(parent, initialize(parent.getEntryPathString(), parent.config, diffs));
		this.config = parent.config;
		this.diffs = diffs;
	}

	private DiffIterator(DiffIterator parent, Diff diff, File binDir) {
		super(parent, Arrays.asList(binDir.listFiles()).stream()
				.map(file -> {
					var mode = file.isDirectory() ? FileMode.TREE : FileMode.REGULAR_FILE;
					return new TreeEntry(file.getName(), mode, diff, file);
				})
				.toList());
		this.config = parent.config;
		this.diffs = new ArrayList<>();
	}

	private static List<TreeEntry> initialize(String prefix, GitConfig config, List<Diff> diffs) {
		var list = new ArrayList<TreeEntry>();
		var added = new HashSet<String>();
		diffs.forEach(d -> {
			var path = d.path();
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

	private static File getBinaryDir(GitConfig config, Diff diff) {
		var filestore = new FileStore(config.database);
		var ref = diff.ref();
		var folder = filestore.getFolder(ref.type, ref.refId);
		if (!folder.exists() || folder.listFiles().length == 0)
			return null;
		return folder;
	}

	public DiffIterator createSubtreeIterator() {
		return createSubtreeIterator(null);
	}

	@Override
	public DiffIterator createSubtreeIterator(ObjectReader reader) {
		var entry = getEntry();
		if (entry.data instanceof Diff diff && entry.file != null)
			return new DiffIterator(this, diff, entry.file);
		var path = getEntryPathString();
		return new DiffIterator(this, diffs.stream()
				.filter(d -> d.path().startsWith(path + "/"))
				.toList());
	}

}
