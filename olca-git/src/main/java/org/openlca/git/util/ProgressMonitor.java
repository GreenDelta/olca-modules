package org.openlca.git.util;

import org.openlca.git.model.ModelRef;

public interface ProgressMonitor {

	public void beginTask(String name, int totalWork);
	public void subTask(String name);
	public void worked(int work);
	public default void subTask(String action, ModelRef ref) {
		subTask(action + " " + ref.type.name().toLowerCase().replace("_", " ") + " " + ref.refId);
	}
	
}
