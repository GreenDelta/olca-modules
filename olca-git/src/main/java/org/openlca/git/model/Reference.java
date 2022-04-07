package org.openlca.git.model;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class Reference extends ModelRef {

	public final String commitId;
	public final ObjectId objectId;

	public Reference(String path, String commitId, ObjectId objectId) {
		super(path);
		this.commitId = commitId;
		this.objectId = objectId;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Reference))
			return false;
		var ref = (Reference) obj;
		return ref.objectId.equals(objectId);
	}

	public String getBinariesPath() {
		if (refId == null)
			return null;
		var path = type.name();
		if (!Strings.nullOrEmpty(category)) {
			path += "/" + category;
		}
		return path + "/" + refId + GitUtil.BIN_DIR_SUFFIX;
	}

}