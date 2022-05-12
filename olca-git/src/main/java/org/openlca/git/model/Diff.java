package org.openlca.git.model;

import java.util.List;

public class Diff {

	public final DiffType type;
	public final Reference left;
	public final Reference right;

	public Diff(DiffType type, Reference left, Reference right) {
		this.type = type;
		this.left = left;
		this.right = right;
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