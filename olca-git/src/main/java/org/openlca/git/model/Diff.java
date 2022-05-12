package org.openlca.git.model;

import java.util.List;

import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.ObjectId;

public class Diff extends ModelRef {

	public final DiffType diffType;
	public final String oldCommitId;
	public final ObjectId oldObjectId;
	public final String newCommitId;
	public final ObjectId newObjectId;

	public Diff(DiffType diffType, Reference oldRef, Reference newRef) {
		super(diffType == DiffType.DELETED ? oldRef : newRef);
		this.diffType = diffType;
		this.oldCommitId = oldRef != null ? oldRef.commitId : null;
		this.oldObjectId = oldRef != null ? oldRef.objectId : ObjectId.zeroId();
		this.newCommitId = newRef != null ? newRef.commitId : null;
		this.newObjectId = newRef != null ? newRef.objectId : ObjectId.zeroId();
	}
	
	public Reference toReference(Side side) {
		if (side == Side.OLD)
			return new Reference(path, oldCommitId, oldObjectId);
		return new Reference(path, newCommitId, newObjectId);
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

}