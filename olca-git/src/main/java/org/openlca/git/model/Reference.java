package org.openlca.git.model;

import java.util.Objects;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.git.util.GitUtil;

public class Reference extends ModelRef {

	public final String commitId;
	public final ObjectId objectId;

	public Reference(String path) {
		this(path, null, null);
	}

	public Reference(String path, String commitId, ObjectId objectId) {
		super(path);
		this.commitId = commitId;
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
		if (!(obj instanceof Reference ref))
			return false;
		if (!super.equals(obj))
			return false;
		if (!Objects.equals(ref.commitId, commitId))
			return false;
		return Objects.equals(ref.objectId, objectId);
	}

	public String getBinariesPath() {
		return GitUtil.toBinDirPath(type, category, refId);
	}

	@Override
	protected String fieldsToString() {
		var s = super.fieldsToString();
		return s + ", commitId=" + commitId + ", objectId=" + ObjectId.toString(objectId);
	}

}