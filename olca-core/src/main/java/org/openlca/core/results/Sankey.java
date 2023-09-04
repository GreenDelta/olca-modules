package org.openlca.core.results;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.providers.ResultProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * An instance of this class contains the underlying graph data structure of
 * a Sankey diagram. It does not contain methods for rendering the diagram on
 * some output but just the data structures and functions for building the
 * graph.
 */
public class Sankey<T> {

	/**
	 * The result reference of the graph; a flow or impact category.
	 */
	public final T reference;

	/**
	 * The root (source or sink, depending on the perspective) of the graph
	 * which always describes the reference product of the underlying product
	 * system.
	 */
	public final Node root;

	/**
	 * The total number of nodes in the graph.
	 */
	public int nodeCount;

	/**
	 * We hold a reference to the solution provider of the underlying result
	 * in order to calculate the link shares.
	 */
	private final ResultProvider solution;

	/**
	 * Describes a single node in the graph. For a process product in the
	 * system there can be only one or no node in the resulting graph. Thus,
	 * a node can be uniquely identified by its process product (which can be
	 * also a waste input) and its index of the tech. matrix.
	 */
	public static class Node {

		/**
		 * The matrix index of corresponding process product.
		 */
		public int index;

		/**
		 * The process product of the node.
		 */
		public TechFlow product;

		/**
		 * The total result (upstream plus direct) of this node in the supply
		 * chain.
		 */
		public double total;

		/**
		 * The direct result of this node.
		 */
		public double direct;

		/**
		 * The absolute share of the total result of this node in relation to
		 * the total result of the root of the graph (of the reference product).
		 */
		public double share;

		/**
		 * The nodes of the providers of products or waste treatment of this
		 * node. Note that depending on the cutoffs, when building the graph
		 * this list is often not complete.
		 */
		public List<Node> providers = new ArrayList<>();

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof Node other))
				return false;
			return index == other.index;
		}

		@Override
		public int hashCode() {
			return index;
		}
	}

	private Sankey(T reference, ResultProvider solution) {
		this.reference = reference;
		this.solution = solution;
		this.root = new Node();
	}

	/**
	 * Creates a new builder of a graph for a Sankey diagram for the given
	 * reference (flow or impact) and result.
	 */
	public static <T> Builder<T> of(T ref, ResultProvider result) {
		return new Builder<>(ref, result);
	}

	/**
	 * Traverses the graph in breadth-first order starting from the root.
	 */
	public void traverse(Consumer<Node> fn) {
		if (fn == null)
			return;
		var queue = new ArrayDeque<Node>(root.providers.size());
		queue.add(root);
		var visited = new TIntHashSet(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
		visited.add(root.index);
		while (!queue.isEmpty()) {
			var node = queue.poll();
			fn.accept(node);
			for (var provider : node.providers) {
				if (visited.contains(provider.index))
					continue;
				queue.add(provider);
				visited.add(provider.index);
			}
		}
	}

	/**
	 * Get the share of the upstream total of the given provider that goes
	 * into the given node. If the provider is only a provider of the given
	 * node, the share is 1. If it is a provider of several nodes (e.g. an
	 * electricity process that provides electricity to several other processes)
	 * the link share is a value between 0 and 1. The link share can also be
	 * a negative value (-1..0) in case of negative upstream contributions.
	 */
	public double getLinkShare(Node provider, Node node) {
		if (provider == null || node == null || provider.index == node.index)
			return 0;
		// the provider is i; the linked node is j: i -> j
		// we calculate the share by dividing the scaled amount of i that goes
		// into j with the total amount of i that is produced in the system
		int i = provider.index;
		int j = node.index;

		// the total amount of i that is produced in the system: s[i] * A[i,i]
		var total = solution.scaledTechValueOf(i, i);
		if (total == 0)
			return 0;

		var linkedAmount = solution.scaledTechValueOf(i, j);
		return linkedAmount == 0 ? 0 : -linkedAmount / total;

		// an alternative calculation is to relate the upstream share to the
		// total amount of i that is produced by the scaled demand of j. This
		// makes the effects of loops sometimes more clear but the upstream
		// shares can be misleading when there are multiple paths of i into j
		// the total amount of i that goes into j: tf[j] * A[i,i] * INV[i,j]
		// var aii = solution.techValueOf(i, i);
		// var inv_ij = solution.solutionOfOne(j)[i];
		// var tfj = solution.totalFactorOf(j);
		// var linkedAmount = tfj * aii * inv_ij;
		// return linkedAmount == 0 ? 0 : linkedAmount / total;
	}

	/**
	 * Returns a string with the graph in
	 * <a href="https://en.wikipedia.org/wiki/DOT_(graph_description_language)">
	 * DOT format</a>.
	 */
	public String toDot() {
		var buf = new StringBuilder();
		buf.append("digraph g {\n")
				.append("  rankdir=BT;\n")
				.append("  node [shape=point];\n")
				.append("  edge [arrowhead=none];\n")
				.append("  ").append(root.index).append(";\n");
		traverse(node -> {
			for (var provider : node.providers) {
				var penwidth = 0.5 + 3
						* provider.share
						* getLinkShare(provider, node);
				buf.append("  ")
						.append(provider.index)
						.append(" -> ")
						.append(node.index)
						.append(" [penwidth=")
						.append(penwidth)
						.append("];\n");
			}
		});
		buf.append("}\n");
		return buf.toString();
	}


	public static class Builder<T> {

		private final Sankey<T> sankey;
		private final ResultProvider result;

		// result references
		private EnviFlow flow;
		private ImpactDescriptor impact;

		// cutoff rules
		private double minShare = 0;
		private int maxNodes = -1;

		private final TIntObjectHashMap<Node> handled;
		private PriorityQueue<Candidate> candidates;

		private Builder(T ref, ResultProvider result) {
			this.sankey = new Sankey<>(ref, result);
			this.result = result;
			handled = new TIntObjectHashMap<>(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1);
		}

		/**
		 * The minimum share of the total result of a node in relation to the
		 * total result of the root of the graph (of the reference product) that
		 * is required for a node to be added to the graph.
		 */
		public Builder<T> withMinimumShare(double share) {
			this.minShare = Math.abs(share);
			return this;
		}

		/**
		 * The maximum number of nodes that should be added to the graph.
		 */
		public Builder<T> withMaximumNodeCount(int count) {
			this.maxNodes = count;
			return this;
		}

		/**
		 * Builds the underlying graph of a Sankey diagram.
		 */
		public Sankey<T> build() {

			// select the result reference
			// TODO: currently no support for cost-results
			if (sankey.reference instanceof EnviFlow) {
				flow = (EnviFlow) sankey.reference;
			} else if (sankey.reference instanceof ImpactDescriptor) {
				impact = (ImpactDescriptor) sankey.reference;
			}

			// create the root node of the reference product
			var root = sankey.root;
			var techIndex = result.techIndex();
			root.product = result.demand().techFlow();
			root.index = techIndex.of(root.product);
			root.total = getTotal(root.product);
			root.direct = getDirect(root.product);
			root.share = root.total == 0 ? 0 : 1;
			sankey.nodeCount = 1;
			handled.put(root.index, root);

			// when the number of nodes is limited, we select the nodes with
			// the highest contributions
			if (maxNodes > 0) {
				candidates = new PriorityQueue<>(
						(n1, n2) -> Double.compare(n2.share, n1.share));
			}

			// expand the graph recursively
			if (root.total != 0 && (maxNodes < 0 || maxNodes > 1)) {
				expand(root);
				fill(root);
			}
			return sankey;
		}

		private double getTotal(TechFlow techFlow) {
			int techIdx = result.indexOf(techFlow);
			if (techIdx < 0)
				return 0;
			if (flow != null) {
				int flowIdx = result.indexOf(flow);
				if (flowIdx < 0)
					return 0;
				double total = result.totalFlowOf(flowIdx, techIdx);
				return ResultProvider.flowValueView(flow, total);
			}
			if (impact != null) {
				int impactIdx = result.indexOf(impact);
				return impactIdx < 0
						? 0
						: result.totalImpactOf(impactIdx, techIdx);
			}
			return 0;
		}

		private double getDirect(TechFlow techFlow) {
			int techIdx = result.indexOf(techFlow);
			if (techIdx < 0)
				return 0;
			if (flow != null) {
				int flowIdx = result.indexOf(flow);
				if (flowIdx < 0)
					return 0;
				var direct = result.directFlowOf(flowIdx, techIdx);
				return ResultProvider.flowValueView(flow, direct);
			}
			if (impact != null) {
				int impactIdx = result.indexOf(impact);
				return impactIdx < 0
						? 0
						: result.directImpactOf(impactIdx, techIdx);
			}
			return 0;
		}

		/**
		 * Expands recursively the providers of the given node, that was already
		 * added to the graph, according to the cutoff rules of this builder.
		 */
		private void expand(Node node) {
			result.iterateTechColumnOf(node.index).eachNonZero((i, $) -> {
				if (i == node.index)
					return;
				var provider = handled.get(i);
				if (provider != null) {
					node.providers.add(provider);
					return;
				}

				// calculate and check the share
				var product = result.techIndex().at(i);
				var total = getTotal(product);
				if (total == 0)
					return;
				var share = Math.abs(total / sankey.root.total);
				if (share < minShare)
					return;

				// construct a candidate node
				provider = new Node();
				provider.index = i;
				provider.direct = getDirect(product);
				provider.total = total;
				provider.product = product;
				provider.share = share;

				// if there is no limit regarding the
				// node count, add and expand the node,
				// use the priority queue otherwise
				if (candidates != null) {
					candidates.add(new Candidate(node, provider));
				} else {
					add(node, provider);
					expand(provider);
				}
			});

			// we add the candidate with the largest share to
			// the providers.
			if (candidates == null || candidates.isEmpty())
				return;
			var next = candidates.poll();
			add(next.handled, next.provider);
			if (sankey.nodeCount < maxNodes) {
				expand(next.provider);
			}
		}

		private void add(Node existing, Node provider) {
			existing.providers.add(provider);
			handled.put(provider.index, provider);
			sankey.nodeCount++;
		}

		/**
		 * Executes the fill phase after the expansion phase: in the expansion phase,
		 * we add node-provider relations in breadth-first order applying cut-off
		 * rules. A node k is then maybe not added as provider of a node i because of
		 * these cut-off rules. But, k is maybe added as provider of a node j later
		 * because it has a higher upstream contribution there. In the fill phase,
		 * we then add such missing relations (k, i) of the existing nodes in the
		 * sub-graph.
		 */
		private void fill(Node root) {
			var queue = new ArrayDeque<Node>();
			queue.add(root);
			var queued = new HashSet<Integer>();
			queued.add(root.index);

			while (!queue.isEmpty()) {
				var next = queue.poll();
				var providers = new HashSet<Integer>();
				for (var provider : next.providers) {
					if (!queued.contains(provider.index)) {
						queued.add(provider.index);
						queue.add(provider);
					}
					providers.add(provider.index);
				}

				result.iterateTechColumnOf(next.index).eachNonZero((i, $) -> {
					if (i == next.index || providers.contains(i))
						return;
					var node = handled.get(i);
					if (node == null)
						return;
					if (!queued.contains(i)) {
						queued.add(i);
						queue.add(node);
					}
					next.providers.add(node);
				});
			}
		}
	}

	/**
	 * Describes a new provider candidate of a handled node that was already
	 * added to the graph. This candidate could be added in a next expansion
	 * step depending on the result share.
	 */
	private static class Candidate {

		/**
		 * The handled node that was already added to the graph.
		 */
		final Node handled;

		/**
		 * The provider node that could be added in a next expansion step.
		 */
		final Node provider;

		/**
		 * The result share of the provider candidate.
		 */
		final double share;

		Candidate(Node handled, Node provider) {
			this.handled = handled;
			this.provider = provider;
			this.share = provider.share;
		}
	}
}
