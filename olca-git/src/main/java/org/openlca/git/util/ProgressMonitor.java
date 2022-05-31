package org.openlca.git.util;

import org.openlca.git.model.ModelRef;

public interface ProgressMonitor {

	void beginTask(String name, int totalWork);

	void subTask(String name);

	void worked(int work);

	default void subTask(String action, ModelRef ref) {
		subTask(action + " " + ref.type.name().toLowerCase().replace("_", " ") + " " + ref.refId);
	}

}
