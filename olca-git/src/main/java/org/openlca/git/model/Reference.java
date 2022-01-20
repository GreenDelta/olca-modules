package org.openlca.git.model;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.core.model.ModelType;
import org.openlca.git.util.GitUtil;

public class Reference {

	public final ModelType type;
	public final String refId;
	public final String fullPath;
	public final String category;
	public final String commitId;
	public final ObjectId objectId;

	public Reference(ModelType type, String refId, String commitId, String fullPath, ObjectId objectId) {
		this.type = type;
		this.refId = refId;
		this.commitId = commitId;
		this.fullPath = GitUtil.decode(fullPath);
		this.objectId = objectId;
		if (fullPath.indexOf("/") == fullPath.lastIndexOf("/")) {
			this.category = "";
		} else {
			String category = fullPath.substring(fullPath.indexOf("/") + 1, fullPath.lastIndexOf("/"));
			this.category = GitUtil.decode(category);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Reference))
			return false;
		var ref = (Reference) obj;
		return ref.objectId.equals(objectId);
	}
	
}