package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class Sankey<T> {

	public final T reference;
	public final Node root;
	public int nodeCount;

	private Sankey(T reference) {
		this.reference = reference;
		this.root = new Node();
	}

	public static class Node {
		public ProcessProduct product;
		public int index;
		public double total;
		public double direct;
		public double share;
		public List<Node> providers = new ArrayList<>();
	}

	public static <T> Builder<T> of(T ref, FullResult result) {
		return new Builder<>(ref, result);
	}

	public static class Builder<T> {

		private final Sankey<T> sankey;
		private final FullResult result;
		double minShare;
		int maxNodes = -1;

		private final TIntObjectHashMap<Node> handled = new TIntObjectHashMap<>();
		private IndexFlow flow;
		private ImpactCategoryDescriptor impact;

		private PriorityQueue<Candidate> queue;

		private Builder(T ref, FullResult result) {
			this.sankey = new Sankey<>(ref);
			this.result = result;
		}

		public Builder<T> withMinimumShare(double share) {
			this.minShare = Math.abs(share);
			return this;
		}

		public Builder<T> withMaximumNodeCount(int count) {
			this.maxNodes = count;
			return this;
		}

		public Sankey<T> build() {
			var root = sankey.root;
			root.product = result.techIndex.getRefFlow();
			root.index = result.techIndex.getIndex(root.product);

			// TODO: currently no support for cost-results
			if (sankey.reference instanceof IndexFlow) {
				flow = (IndexFlow) sankey.reference;
			} else if (sankey.reference instanceof ImpactCategoryDescriptor) {
				impact = (ImpactCategoryDescriptor) sankey.reference;
			}

			root.total = getTotal(root.product);
			root.direct = getDirect(root.product);
			root.share = root.total == 0 ? 0 : 1;
			sankey.nodeCount = 1;
			handled.put(root.index, root);

			// expand the graph
			if (root.total != 0 && (maxNodes < 0 || maxNodes > 1)) {
				expand(root);
			}

			return sankey;
		}

		private double getTotal(ProcessProduct product) {
			if (flow != null)
				return result.getUpstreamFlowResult(product, flow);
			if (impact != null)
				return result.getUpstreamImpactResult(product, impact);
			return 0;
		}

		private double getDirect(ProcessProduct product) {
			if (flow != null)
				return result.getDirectFlowResult(product, flow);
			if (impact != null)
				return result.getDirectImpactResult(product, impact);
			return 0;
		}

		private void expand(Node node) {
			var colA = result.solutions.columnOfA(node.index);
			for (int i = 0; i < colA.length; i++) {
				if (i == node.index || colA[i] == 0)
					continue;
				var provider = handled.get(i);
				if (provider != null) {
					node.providers.add(provider);
					continue;
				}

				// calculate and check the share
				var product = result.techIndex.getProviderAt(i);
				var total = getTotal(product);
				if (total == 0)
					continue;
				var share = Math.abs(total / sankey.root.total);
				if (share < minShare)
					continue;

				// construct a candidate node
				provider = new Node();
				provider.index = i;
				provider.direct = getDirect(product);
				provider.total = total;
				provider.product = product;
				provider.share = share;

				// if there is no limit regarding the
				// node count, add and expand the node
				if (maxNodes < 0) {
					add(node, provider);
					expand(provider);
					continue;
				}

				// add is as a candidate
				if (queue == null) {
					queue = new PriorityQueue<>(
							(n1, n2) -> Double.compare(n2.share, n1.share));
				}
				queue.add(Candidate.of(node, provider));
			}

			// we add the candidate with the largest share to
			// the providers.
			if (queue == null || queue.isEmpty())
				return;
			var best = queue.poll();
			add(best.handled, best.provider);
			if (sankey.nodeCount < maxNodes ) {
				expand(best.provider);
			}
		}

		private void add(Node existing, Node provider) {
			existing.providers.add(provider);
			handled.put(provider.index, provider);
			sankey.nodeCount++;
		}
	}

	private static class Candidate {
		Node handled;
		Node provider;
		double share;

		static Candidate of(Node handled, Node provider) {
			var cand = new Candidate();
			cand.handled = handled;
			cand.provider = provider;
			cand.share = provider.share;
			return cand;
		}
	}
}
