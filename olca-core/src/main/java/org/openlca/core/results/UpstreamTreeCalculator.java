package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

class UpstreamTreeCalculator {

	private FullResult result;
	private LinkContributions linkContributions;
	private Multimap<LongPair, LongPair> links;
	private boolean skipNegatives = false;
	private boolean skipNulls = false;

	public UpstreamTreeCalculator(FullResult result) {
		this.result = result;
		this.linkContributions = result.linkContributions;
		this.links = makeLinks(result.productIndex);
	}

	private Multimap<LongPair, LongPair> makeLinks(TechIndex index) {
		Multimap<LongPair, LongPair> links = ArrayListMultimap.create();
		for (LongPair exchange : index.getLinkedExchanges()) {
			long processId = exchange.getFirst();
			LongPair provider = index.getLinkedProvider(exchange);
			for (LongPair recipient : index.getProviders(processId)) {
				links.put(recipient, provider);
			}
		}
		return links;
	}

	public void skipNegativeValues(boolean skipNegatives) {
		this.skipNegatives = skipNegatives;
	}

	public void skipNullValues(boolean skipNulls) {
		this.skipNulls = skipNulls;
	}

	public UpstreamTree calculate(FlowDescriptor flow) {
		FlowResultFetch fn = new FlowResultFetch(flow);
		return calculate(fn);
	}

	public UpstreamTree calculate(ImpactCategoryDescriptor impact) {
		ImpactResultFetch fn = new ImpactResultFetch(impact);
		return calculate(fn);
	}

	public UpstreamTree calculateCosts() {
		CostResultFetch fn = new CostResultFetch();
		return calculate(fn);
	}

	private UpstreamTree calculate(ResultFetch fn) {

		UpstreamTree tree = new UpstreamTree();
		tree.setReference(fn.getReference());
		UpstreamTreeNode root = new UpstreamTreeNode();
		LongPair refProduct = result.productIndex.getRefFlow();
		root.setShare(1d);
		root.setProcessProduct(refProduct);
		root.setAmount(fn.getTotalAmount(refProduct));
		tree.setRoot(root);

		NodeSorter sorter = new NodeSorter();
		Stack<UpstreamTreeNode> stack = new Stack<>();
		stack.push(root);
		HashSet<LongPair> handled = new HashSet<>();
		handled.add(refProduct);

		while (!stack.isEmpty()) {

			UpstreamTreeNode node = stack.pop();
			List<UpstreamTreeNode> childs = createChildNodes(node, fn);
			Collections.sort(childs, sorter);
			node.getChildren().addAll(childs);

			for (int i = childs.size() - 1; i >= 0; i--) {
				// push in reverse order, so that the highest contribution is
				// on the top
				UpstreamTreeNode child = childs.get(i);
				if (!handled.contains(child.getProcessProduct())) {
					stack.push(child);
					handled.add(child.getProcessProduct());
				}
			}
		}
		return tree;
	}

	private List<UpstreamTreeNode> createChildNodes(UpstreamTreeNode parent,
			ResultFetch fn) {
		List<UpstreamTreeNode> childNodes = new ArrayList<>();
		LongPair recipient = parent.getProcessProduct();
		for (LongPair provider : links.get(recipient)) {
			double share = linkContributions.getShare(provider, recipient)
					* parent.getShare();
			double amount = share * fn.getTotalAmount(provider);
			if (amount == 0 && skipNulls)
				continue;
			if (amount < 0 && skipNegatives)
				continue;
			UpstreamTreeNode node = new UpstreamTreeNode();
			node.setShare(share);
			node.setAmount(amount);
			node.setProcessProduct(provider);
			childNodes.add(node);
		}
		return childNodes;
	}

	private interface ResultFetch {

		double getTotalAmount(LongPair product);

		BaseDescriptor getReference();
	}

	private class FlowResultFetch implements ResultFetch {

		private final FlowDescriptor flow;
		private final long flowId;
		private final boolean isInput;

		public FlowResultFetch(FlowDescriptor flow) {
			this.flow = flow;
			this.flowId = flow.getId();
			this.isInput = result.flowIndex.isInput(flowId);
		}

		@Override
		public double getTotalAmount(LongPair product) {
			double val = result.getUpstreamFlowResult(product, flowId);
			if (val == 0)
				return 0; // avoid -0 in results
			return isInput ? -val : val;
		}

		@Override
		public BaseDescriptor getReference() {
			return flow;
		}
	}

	private class ImpactResultFetch implements ResultFetch {

		private final ImpactCategoryDescriptor impact;
		private final long impactId;

		public ImpactResultFetch(ImpactCategoryDescriptor impact) {
			this.impact = impact;
			this.impactId = impact.getId();
		}

		@Override
		public double getTotalAmount(LongPair processProduct) {
			return result.getUpstreamImpactResult(processProduct, impactId);
		}

		@Override
		public BaseDescriptor getReference() {
			return impact;
		}
	}

	private class CostResultFetch implements ResultFetch {

		@Override
		public double getTotalAmount(LongPair product) {
			return result.getUpstreamCostResult(product);
		}

		@Override
		public BaseDescriptor getReference() {
			BaseDescriptor d = new BaseDescriptor();
			d.setId(0);
			d.setType(ModelType.CURRENCY);
			return d;
		}
	}

	private class NodeSorter implements Comparator<UpstreamTreeNode> {
		@Override
		public int compare(UpstreamTreeNode node1, UpstreamTreeNode node2) {
			return Double.compare(node2.getAmount(), node1.getAmount());
		}
	}

}
