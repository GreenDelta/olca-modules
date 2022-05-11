package org.openlca.git.model;

import java.util.Objects;

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
		this.refId = path.endsWith(".json")
				? path.substring(
						path.contains("/")
								? path.lastIndexOf("/") + 1
								: 0,
						path.lastIndexOf("."))
				: null;
	}

	public ModelRef(ModelRef ref) {
		this.path = ref.path;
		this.type = ref.type;
		this.refId = ref.refId;
		this.category = ref.category;
	}

	@Override
	public int hashCode() {
		return path != null ? path.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ModelRef))
			return false;
		var other = (ModelRef) o;
		return Objects.equals(path, other.path);
	}

}
