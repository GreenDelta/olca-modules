package org.openlca.ilcd.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyTraversal {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final DataStore store;

	public DependencyTraversal(DataStore store) {
		this.store = store;
	}

	/**
	 * Visits all data sets that are reachable from the given data set reference
	 * and calls the consumer function for each of these data sets including the
	 * data set of the start-reference.
	 */
	public void on(Ref start, Consumer<IDataSet> fn) {
		if (store == null || start == null || fn == null)
			return;
		ArrayList<Ref> visited = new ArrayList<>();
		ArrayDeque<Ref> deque = new ArrayDeque<>();
		deque.add(start);
		while (!deque.isEmpty()) {
			Ref next = deque.poll();
			visited.add(next);
			try {
				IDataSet ds = store.get(next.getDataSetClass(), next.uuid);
				if (ds == null) {
					log.warn("could not get data set for {}", next);
					continue;
				}
				fn.accept(ds);
				for (Ref dep : RefTree.create(ds).getRefs()) {
					if (visited.contains(dep) || deque.contains(dep))
						continue;
					deque.add(dep);
				}
			} catch (Exception e) {
				log.error("failed to get dependencies for {}", next, e);
			}
		}
	}
}
