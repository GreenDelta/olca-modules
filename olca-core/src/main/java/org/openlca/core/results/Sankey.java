package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class Sankey<T> {

	public final T reference;
	public final Node root;
	public double minShare;
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

		private final HashSet<ProcessProduct> handled = new HashSet<>();
		private IndexFlow flow;
		private ImpactCategoryDescriptor impact;

		private Builder(T ref, FullResult result) {
			this.sankey = new Sankey<>(ref);
			this.result = result;
		}

		public Builder<T> withMinimumShare(double share) {
			this.minShare = share;
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
			sankey.nodeCount = 1;
			sankey.minShare = 1;
			if (root.total != 0) {
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
	}


}
