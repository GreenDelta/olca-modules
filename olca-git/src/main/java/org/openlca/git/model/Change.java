package org.openlca.git.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Change extends ModelRef {

	public final ChangeType changeType;

	private Change(ChangeType changeType, ModelRef ref) {
		super(ref);
		this.changeType = changeType;
	}

	private Change(String path) {
		super(path);
		this.changeType = ChangeType.MODIFY;
	}

	private static Change of(ChangeType type, ModelRef ref) {
		if (ref.isLibrary)
			return new Change(ref.path.substring(0, ref.path.indexOf("/")));
		return new Change(type, ref);
	}
	
	@Override
	protected String fieldsToString() {
		var s = super.fieldsToString();
		return s + ", changeType=" + changeType;
	}

	public static Change add(ModelRef ref) {
		return of(ChangeType.ADD, ref);
	}

	public static Change modify(ModelRef ref) {
		return of(ChangeType.MODIFY, ref);
	}

	public static Change delete(ModelRef ref) {
		return of(ChangeType.DELETE, ref);
	}

	public static List<Change> move(ModelRef oldRef, ModelRef newRef) {
		return Arrays.asList(delete(oldRef), add(newRef));
	}

	public static Set<Change> of(Diff diff) {
		return of(Arrays.asList(diff));
	}

	public static Set<Change> of(List<Diff> diffs) {
		var changes = new HashSet<Change>();
		for (var diff : diffs) {
			if (diff.diffType == DiffType.ADDED) {
				changes.add(add(diff.newRef));
			} else if (diff.diffType == DiffType.MODIFIED) {
				changes.add(modify(diff.newRef));
			} else if (diff.diffType == DiffType.DELETED) {
				changes.add(delete(diff.oldRef));
			} else if (diff.diffType == DiffType.MOVED) {
				changes.addAll(move(diff.oldRef, diff.newRef));
			}
		}
		return changes;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Change c))
			return false;
		if (c.changeType != changeType)
			return false;
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return (changeType.name() + "/" + path).hashCode();
	}

	public enum ChangeType {

		ADD,

		MODIFY,

		DELETE;

	}

}
