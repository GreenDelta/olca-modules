package org.openlca.ilcd.util;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.io.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visits all data sets that are reachable starting from a given data
 * set reference and calls a consumer function for each of these data
 * sets including for the data set of the start-reference.
 */
public class DependencyTraversal {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final DataStore store;
	private final Ref start;
	private Predicate<Ref> filter;

	private DependencyTraversal(DataStore store, Ref start) {
		this.store = Objects.requireNonNull(store);
		this.start = Objects.requireNonNull(start);
	}

	public static DependencyTraversal of(DataStore store, Ref start) {
		return new DependencyTraversal(store, start);
	}

	/**
	 * Only include and follow dependencies that match the given
	 * predicate.
	 */
	public DependencyTraversal filter(Predicate<Ref> p) {
		this.filter = p;
		return this;
	}

	public void forEach(Consumer<IDataSet> fn) {
		if (fn == null)
			return;
		var visited = new HashSet<Ref>();
		var deque = new ArrayDeque<Ref>();
		deque.add(start);
		visited.add(start);

		while (!deque.isEmpty()) {
			Ref next = deque.poll();
			try {
				var dataSet = store.get(next.getDataSetClass(), next.uuid);
				if (dataSet == null) {
					log.warn("could not get data set for {}", next);
					continue;
				}
				fn.accept(dataSet);
				for (Ref dep : RefTree.create(dataSet).getRefs()) {
					if (visited.contains(dep))
						continue;
					visited.add(dep);
					if (filter != null && !filter.test(dep))
						continue;
					deque.add(dep);
				}
			} catch (Exception e) {
				log.error("failed to get dependencies for {}", next, e);
			}
		}
	}
}
