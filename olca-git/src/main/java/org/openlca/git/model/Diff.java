package org.openlca.git.model;

import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.FileMode;

public class Diff {

	public final DiffType type;
	public final Reference left;
	public final Reference right;

	public Diff(DiffEntry entry) {
		this(entry, null, null);
	}

	public Diff(DiffEntry entry, String leftCommitId, String rightCommitId) {
		this.type = DiffType.forChangeType(entry.getChangeType());
		this.left = toCommitReference(leftCommitId, entry, Side.OLD);
		this.right = toCommitReference(rightCommitId, entry, Side.NEW);
	}

	public Diff(DiffType type, Reference left, Reference right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	private Reference toCommitReference(String commitId, DiffEntry entry, Side side) {
		if (entry.getMode(side) == FileMode.MISSING)
			return null;
		var path = entry.getPath(side);
		var objectId = entry.getId(side).toObjectId();
		return new Reference(path, commitId, objectId);
	}

	public ModelRef ref() {
		if (right != null)
			return right;
		return left;
	}

	public String path() {
		return type == DiffType.DELETED ? left.path : right.path;
	}

	public static List<Diff> filter(List<Diff> diffs, DiffType type) {
		return filter(diffs, new DiffType[] { type });
	}

	public static List<Diff> filter(List<Diff> diffs, DiffType... types) {
		if (types == null)
			return diffs;
		return diffs.stream().filter(d -> {
			for (DiffType type : types)
				if (d.type == type)
					return true;
			return false;
		}).toList();
	}

}