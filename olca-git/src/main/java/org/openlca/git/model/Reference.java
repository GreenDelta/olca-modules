package org.openlca.git.model;

import java.util.Objects;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class Reference extends ModelRef {

	public final String commitId;
	public final ObjectId objectId;

	public Reference(String path, String commitId, ObjectId objectId) {
		super(path);
		this.commitId = commitId != null ? commitId : "";
		this.objectId = objectId != null ? objectId : ObjectId.zeroId();
	}

	@Override
	public int hashCode() {
		return (commitId + ":" + path).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Reference))
			return false;
		var ref = (Reference) obj;
		if (!super.equals(obj))
			return false;
		if (!ref.commitId.equals(commitId))
			return false;
		return Objects.equals(ref.objectId, objectId);
	}

	public String getBinariesPath() {
		if (refId == null || type == null)
			return null;
		var path = type.name();
		if (!Strings.nullOrEmpty(category)) {
			path += "/" + category;
		}
		return path + "/" + refId + GitUtil.BIN_DIR_SUFFIX;
	}

}