package org.openlca.cloud.api;

public interface FetchNotifier {

	void beginTask(TaskType type, int total);

	void worked();

	void endTask();

	enum TaskType {

		FETCH, PULL;

	}

}
