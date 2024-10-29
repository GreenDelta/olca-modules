package org.openlca.git.model;

import java.util.List;

public class Diff extends ModelRef {

	public final DiffType diffType;
	public final Reference oldRef;
	public final Reference newRef;

	public Diff(DiffType diffType, Reference oldRef, Reference newRef) {
		super(diffType == DiffType.DELETED ? oldRef : newRef);
		this.diffType = diffType;
		this.oldRef = oldRef;
		this.newRef = newRef;
	}

	public static Diff added(Reference newRef) {
		return new Diff(DiffType.ADDED, null, newRef);
	}

	public static Diff modified(Reference oldRef, Reference newRef) {
		return new Diff(DiffType.MODIFIED, oldRef, newRef);
	}

	public static Diff moved(Reference oldRef, Reference newRef) {
		return new Diff(DiffType.MOVED, oldRef, newRef);
	}

	public static Diff deleted(Reference oldRef) {
		return new Diff(DiffType.DELETED, oldRef, null);
	}

	public static List<Diff> filter(List<Diff> diffs, DiffType type) {
		return filter(diffs, new DiffType[] { type });
	}

	public static List<Diff> filter(List<Diff> diffs, DiffType... types) {
		if (types == null)
			return diffs;
		return diffs.stream().filter(d -> {
			for (DiffType diffType : types)
				if (d.diffType == diffType)
					return true;
			return false;
		}).toList();
	}

	@Override
	protected String fieldsToString() {
		var s = super.fieldsToString();
		return s + ", diffType=" + diffType
				+ ", oldRef=" + (oldRef != null ? oldRef.toString() : "null")
				+ ", newRef=" + (newRef != null ? newRef.toString() : "null");
	}

}