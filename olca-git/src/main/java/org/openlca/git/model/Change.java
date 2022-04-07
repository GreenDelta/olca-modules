package org.openlca.git.model;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class Change extends ModelRef {

	public final ChangeType changeType;

	public Change(DiffEntry e) {
		super(e);
		this.changeType = e.getChangeType();
	}

	public Change(ChangeType changeType, String path) {
		super(path);
		this.changeType = changeType;
	}

}