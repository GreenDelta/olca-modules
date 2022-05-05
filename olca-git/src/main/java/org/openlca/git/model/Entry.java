package org.openlca.git.model;

import org.eclipse.jgit.lib.ObjectId;

public class Entry extends Reference {

	public final String name;
	public final EntryType typeOfEntry;

	public Entry(String path, String commitId, ObjectId objectId) {
		super(path, commitId, objectId);
		this.name = this.path.contains("/") ? this.path.substring(this.path.lastIndexOf("/") + 1) : this.path;
		if (!this.path.contains("/")) {
			typeOfEntry = EntryType.MODEL_TYPE;
		} else if (this.refId == null) {
			typeOfEntry = EntryType.CATEGORY;
		} else {
			typeOfEntry = EntryType.DATASET;
		}
	}

	public static enum EntryType {

		MODEL_TYPE,
		CATEGORY,
		DATASET;

	}
}