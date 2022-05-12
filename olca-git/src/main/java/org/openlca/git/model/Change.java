package org.openlca.git.model;

public class Change extends ModelRef {

	public DiffType diffType;

	public Change(Diff diff) {
		this(diff.diffType, diff);
	}
	
	public Change(DiffType diffType, ModelRef ref) {
		super(ref);
		this.diffType = diffType;
	}

}
