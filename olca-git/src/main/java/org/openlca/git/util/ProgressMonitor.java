package org.openlca.git.util;

import org.openlca.core.model.ModelType;
import org.openlca.git.model.ModelRef;

public interface ProgressMonitor {

	static ProgressMonitor NULL = new ProgressMonitor() {
	};

	default void beginTask(String name) {
		beginTask(name, -1);
	}

	default void beginTask(String name, int totalWork) {
	}

	default void subTask(String name) {
	}

	default void subTask(ModelRef ref) {
		if (ref.isCategory) {
			subTask("Category " + ref.path.substring(ref.path.indexOf("/") + 1));
		} else {
			subTask(getLabel(ref.type) + " " + ref.refId);
		}
	}

	default void worked(int work) {
	}

	private static String getLabel(ModelType type) {
		if (type == null)
			return "";
		return type.name().charAt(0) + type.name().substring(1).toLowerCase().replace("_", " ");
	}

}
