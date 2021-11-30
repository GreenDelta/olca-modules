package org.openlca.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.index.LongPair;

/**
 * An implementation of Kahn's algorithm for topological sorting.
 */
public final class TopoSort {

	private TopoSort() {
	}

	/**
	 * Creates a topological order of the given relations. Each relation is
	 * encoded as a pair of IDs {@code (first, second)} where the (element with
	 * the) {@code second} ID depends on the (element with the) {@code first} ID;
	 * e.g. {@code (1, 2)} means that 2 depends on 1. The IDs are returned in a
	 * list with increasing dependency order. As a topological order can be only
	 * created on relations that form an acyclic graph, the first element of the
	 * returned list has no dependencies. When there are cycles in the relations,
	 * this method returns {@code null} which means that a topological order
	 * cannot be created.
	 */
	public static List<Long> of(Iterable<LongPair> pairs) {

		// We first build the dependency graph from
		// the given pairs. The IDs of the pairs are
		// our nodes. We store the in-degree and the
		// successors of each node:
		Set<Long> nodes = new HashSet<>();
		Map<Long, Integer> inDegrees = new HashMap<>();
		Map<Long, List<Long>> successors = new HashMap<>();
		for (LongPair pair : pairs) {
			if (nodes.add(pair.first())) {
				inDegrees.put(pair.first(), 0);
			}
			if (nodes.add(pair.second())) {
				inDegrees.put(pair.second(), 0);
			}
			List<Long> succ = successors.computeIfAbsent(
				pair.first(), k -> new ArrayList<>());
			succ.add(pair.second());
			inDegrees.put(pair.second(), inDegrees.get(pair.second()) + 1);
		}

		// We remove a node with indegree=0 from the
		// graph with and add it to the ordered
		// sequence until there are no more nodes
		// in the graph.
		List<Long> ordered = new ArrayList<>();
		while (!nodes.isEmpty()) {

			// find the node with indegree=0
			Long node = null;
			for (Map.Entry<Long, Integer> indeg : inDegrees.entrySet()) {
				if (indeg.getValue() == 0) {
					node = indeg.getKey();
					break;
				}
			}

			if (node == null) {
				// could not find a node with indegree=0
				// -> there are cycles in the graph
				return null;
			}

			// we found a node and remove it from the
			// graph
			ordered.add(node);
			nodes.remove(node);
			inDegrees.remove(node);
			List<Long> succ = successors.remove(node);
			if (succ != null) {
				for (Long s : succ) {
					inDegrees.put(s, inDegrees.get(s) - 1);
				}
			}
		}
		return ordered;
	}
}
