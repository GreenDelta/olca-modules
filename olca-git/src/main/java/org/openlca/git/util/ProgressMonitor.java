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
		var type = ref.isCategory
				? "Category"
				: getLabel(ref.type);
		var path = ref.path.substring(ref.path.indexOf("/") + 1);
		subTask(type + " " + path);
	}

	default void worked(int work) {
	}

	default boolean isCanceled() {
		return false;
	}

	private static String getLabel(ModelType type) {
		if (type == null)
			return "";
		return type.name().charAt(0) + type.name().substring(1).toLowerCase().replace("_", " ");
	}

}
