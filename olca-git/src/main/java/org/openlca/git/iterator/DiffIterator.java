package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.git.model.Diff;
import org.openlca.util.Strings;

public class DiffIterator extends EntryIterator {

	private final List<Diff> diffs;

	public DiffIterator(List<Diff> diffs) {
		super(initialize(null, diffs));
		this.diffs = diffs;
	}

	private DiffIterator(DiffIterator parent, List<Diff> diffs) {
		super(parent, initialize(parent.getEntryPathString(), diffs));
		this.diffs = diffs;
	}

	private static List<TreeEntry> initialize(String prefix, List<Diff> diffs) {
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
			}
			added.add(name);
		});
		return list;
	}

	public DiffIterator createSubtreeIterator() {
		return createSubtreeIterator(null);
	}

	@Override
	public DiffIterator createSubtreeIterator(ObjectReader reader) {
		return new DiffIterator(this, diffs.stream()
				.filter(d -> d.path().startsWith(getEntryPathString() + "/"))
				.toList());
	}

}
