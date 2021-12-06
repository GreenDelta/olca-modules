package org.openlca.ipc.handlers;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openlca.core.results.UpstreamNode;
import org.openlca.core.results.UpstreamTree;

class Upstream {

	static List<UpstreamNode> calculate(UpstreamTree tree, List<StringPair> ids) {
		Collections.reverse(ids);
		Stack<StringPair> path = new Stack<>();
		path.addAll(ids);
		List<UpstreamNode> results = Collections.singletonList(tree.root);
		pathLoop: while (!path.isEmpty()) {
			StringPair next = path.pop();
			for (UpstreamNode result : results) {
				if (!result.provider().provider().refId.equals(next.first))
					continue;
				if (!result.provider().flow().refId.equals(next.second))
					continue;
				results = tree.childs(result);
				continue pathLoop;
			}
			throw new IllegalArgumentException("No upstream results available for given path");
		}
		return results;
	}

	static class StringPair {

		final String first;
		final String second;

		StringPair(String first, String second) {
			this.first = first;
			this.second = second;
		}

	}

}
