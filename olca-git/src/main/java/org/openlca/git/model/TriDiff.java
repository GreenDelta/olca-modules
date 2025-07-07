package org.openlca.git.model;

import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.GitUtil;

/**
 * Used to compare three states of the same ModelRef, e.g. HEAD state, workspace
 * state and remote state of a model.<br>
 */
public class TriDiff extends ModelRef {

	public final Diff left;
	public final Diff right;

	public TriDiff(Diff left, Diff right) {
		super(getPath(left, right));
		this.left = left;
		this.right = right;
	}

	private static String getPath(Diff left, Diff right) {
		var any = right != null ? right : left;
		if (any.isEmptyCategory)
			return any.path + "/" + GitUtil.EMPTY_CATEGORY_FLAG;
		return any.path;
	}

	public boolean isEqual() {
		if (left == null && right == null)
			return true;
		if (left == null || right == null)
			return false;
		if (left.diffType == DiffType.DELETED)
			return right.diffType == DiffType.DELETED;
		return right.diffType != DiffType.DELETED && hasEqualObjectId();
	}

	public boolean isConflict() {
		if (left == null || right == null)
			return false;
		return !isEqual();
	}

	public boolean equalsWorkspace(ClientRepository repo) {
		return right != null && repo.equalsWorkspace(right.newRef);
	}

	private boolean hasEqualObjectId() {
		if (left.newRef == null && right.newRef == null)
			return true;
		if (left.newRef == null || right.newRef == null)
			return false;
		return left.newRef.objectId.equals(right.newRef.objectId);
	}

}
