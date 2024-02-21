package org.openlca.git.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry.Side;

public class Change extends ModelRef {

	public final ChangeType changeType;

	private Change(ChangeType changeType, ModelRef ref) {
		super(ref);
		this.changeType = changeType;
	}

	@Override
	protected String fieldsToString() {
		var s = super.fieldsToString();
		return s + ", changeType=" + changeType;
	}

	public static Change add(ModelRef ref) {
		return new Change(ChangeType.ADD, ref);
	}

	public static Change modify(ModelRef ref) {
		return new Change(ChangeType.MODIFY, ref);
	}

	public static Change delete(ModelRef ref) {
		return new Change(ChangeType.DELETE, ref);
	}

	public static List<Change> move(ModelRef oldRef, ModelRef newRef) {
		return Arrays.asList(delete(oldRef), add(newRef));
	}

	public static List<Change> of(Diff diff) {
		return of(Arrays.asList(diff));
	}

	public static List<Change> of(List<Diff> diffs) {
		var changes = new ArrayList<Change>();
		for (var diff : diffs) {
			if (diff.diffType == DiffType.ADDED) {
				changes.add(add(diff.toReference(Side.NEW)));
			} else if (diff.diffType == DiffType.MODIFIED) {
				changes.add(modify(diff.toReference(Side.NEW)));
			} else if (diff.diffType == DiffType.DELETED) {
				changes.add(delete(diff.toReference(Side.OLD)));
			} else if (diff.diffType == DiffType.MOVED) {
				changes.addAll(move(diff.toReference(Side.OLD), diff.toReference(Side.NEW)));
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
