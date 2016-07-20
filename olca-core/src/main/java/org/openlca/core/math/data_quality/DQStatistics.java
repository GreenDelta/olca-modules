package org.openlca.core.math.data_quality;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class DQStatistics {

	Map<Integer, Integer> processCounts = new HashMap<>();
	Map<Long, Map<Integer, Integer>> exchangeCounts = new HashMap<>();

	DQStatistics() {

	}

	public int getNoOfProcesses() {
		return get(processCounts, 0);
	}

	public int getNoOfProcesses(DQIndicator indicator) {
		return get(processCounts, indicator.position);
	}

	public int getNoOfExchanges() {
		return get(exchangeCounts, 0l, 0);
	}

	public int getNoOfExchanges(ProcessDescriptor process) {
		return get(exchangeCounts, process.getId(), 0);
	}

	public int getNoOfExchanges(DQIndicator indicator) {
		return get(exchangeCounts, 0l, indicator.position);
	}

	public int getNoOfExchanges(ProcessDescriptor process, DQIndicator indicator) {
		return get(exchangeCounts, process.getId(), indicator.position);
	}

	private <T> int get(Map<T, Integer> map, T key) {
		if (map == null || !map.containsKey(key))
			return 0;
		return map.get(key);
	}

	private <T1, T2> int get(Map<T1, Map<T2, Integer>> map, T1 key1, T2 key2) {
		if (!map.containsKey(key1))
			return 0;
		Map<T2, Integer> innerMap = map.get(key1);
		return get(innerMap, key2);
	}
}
