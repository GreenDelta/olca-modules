package org.openlca.core.results.agroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.matrix.index.TechFlow;

record PathNode(PathNode prev, Node node, List<PathNode> branches) {

	static PathNode rootOf(Node r) {
		return new PathNode(null, r, new ArrayList<>());
	}

	boolean isRoot() {
		return prev == null;
	}

	boolean isLeaf() {
		return branches.isEmpty();
	}

	PathNode add(Node n) {
		var branch = new PathNode(this, n, new ArrayList<>());
		branches.add(branch);
		return branch;
	}

	boolean contains(TechFlow techFlow) {
		return Objects.equals(techFlow, node.techFlow())
			|| (!isRoot() && prev.contains(techFlow));
	}

	long providerId() {
		return node.techFlow().providerId();
	}

	String group() {
		return node.group();
	}

	double amount() {
		return node.amount();
	}

	int index() {
		return node.index();
	}

}

