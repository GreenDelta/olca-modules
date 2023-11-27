package org.openlca.git.util;

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

	default void worked(int work) {
	}

}
