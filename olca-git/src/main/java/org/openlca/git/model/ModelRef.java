package org.openlca.git.model;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.openlca.core.model.ModelType;
import org.openlca.git.util.GitUtil;

public class ModelRef {

	public final String path;
	public final ModelType type;
	public final String refId;
	public final String category;

	public ModelRef(DiffEntry e) {
		this(e.getChangeType() == ChangeType.DELETE ? e.getOldPath() : e.getNewPath());
	}

	public ModelRef(String path) {
		this.path = GitUtil.decode(path);
		this.type = ModelType.valueOf(
				path.contains("/")
						? path.substring(0, path.indexOf("/"))
						: path);
		path = path.substring(path.indexOf("/") + 1);
		this.category = path.contains("/")
				? path.substring(0, path.lastIndexOf("/"))
				: "";
		this.refId = path.substring(
				path.contains("/")
						? path.lastIndexOf("/") + 1
						: 0,
				path.lastIndexOf("."));
	}

	public ModelRef(ModelRef ref) {
		this.path = ref.path;
		this.type = ref.type;
		this.refId = ref.refId;
		this.category = ref.category;
	}

}
