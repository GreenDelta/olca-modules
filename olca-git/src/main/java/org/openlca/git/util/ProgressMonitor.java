package org.openlca.git.util;

import org.openlca.git.model.ModelRef;

public interface ProgressMonitor {
	
	static ProgressMonitor NULL = new ProgressMonitor() {
	};

	default void beginTask(String name, int totalWork) {
	}

	default void subTask(String name) {
	}

	default void worked(int work) {
	}

	default void subTask(String action, ModelRef ref) {
		subTask(action + " " + ref.type.name().toLowerCase().replace("_", " ") + " " + ref.refId);
	}

}
