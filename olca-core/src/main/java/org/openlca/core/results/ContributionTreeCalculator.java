package org.openlca.core.results;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Calculates a contribution tree of the processes in a product system to a flow
 * or impact assessment result.
 */
class ContributionTreeCalculator {

	private AnalysisResult result;
	private LinkContributions linkContributions;
	private boolean skipNegatives = false;
	private boolean skipNulls = false;
	private ProtoNode root;

	public ContributionTreeCalculator(AnalysisResult result,
			LinkContributions linkContributions) {
		this.result = result;
		this.linkContributions = linkContributions;
		buildProtoTree();
	}

	private void buildProtoTree() {

		ProductIndex index = result.getProductIndex();
		LongPair refProduct = index.getRefProduct();
		root = new ProtoNode(refProduct, 1d);
		Queue<ProtoNode> queue = new ArrayDeque<>();
		queue.add(root);
		Set<LongPair> handled = new HashSet<>();
		Multimap<LongPair, LongPair> links = makeLinks(index);

		while (!queue.isEmpty()) {
			ProtoNode node = queue.poll();
			handled.add(node.product);
			for (LongPair provider : links.get(node.product)) {
				double linkShare = linkContributions.getShare(provider,
						node.product);
				double share = linkShare * node.share;
				ProtoNode child = new ProtoNode(provider, share);
				node.childs.add(child);
				if (!handled.contains(provider) && !queue.contains(child))
					queue.add(child);
			}
		}
	}

	private Multimap<LongPair, LongPair> makeLinks(ProductIndex index) {
		Multimap<LongPair, LongPair> links = ArrayListMultimap.create();
		for (LongPair input : index.getLinkedInputs()) {
			long recipientProcess = input.getFirst();
			LongPair provider = index.getLinkedOutput(input);
			for (LongPair recipient : index.getProducts(recipientProcess))
				links.put(recipient, provider);
		}
		return links;
	}

	public void skipNegativeValues(boolean skipNegatives) {
		this.skipNegatives = skipNegatives;
	}

	public void skipNullValues(boolean skipNulls) {
		this.skipNulls = skipNulls;
	}

	public ContributionTree calculate(FlowDescriptor flow) {
		FlowResultFetch fn = new FlowResultFetch(flow);
		return calculate(fn);
	}

	public ContributionTree calculate(ImpactCategoryDescriptor impact) {
		ImpactResultFetch fn = new ImpactResultFetch(impact);
		return calculate(fn);
	}

	private ContributionTree calculate(ResultFetch fn) {
		ContributionTree tree = new ContributionTree();
		tree.setReference(fn.getReference());
		ContributionTreeNode node = createNode(root, fn);
		tree.setRoot(node);
		Pair<ProtoNode, ContributionTreeNode> rootPair = new Pair<>(root, node);
		Queue<Pair<ProtoNode, ContributionTreeNode>> queue = new ArrayDeque<>();
		queue.add(rootPair);

		while (!queue.isEmpty()) {
			Pair<ProtoNode, ContributionTreeNode> pair = queue.poll();
			for (ProtoNode childProto : pair.getFirst().childs) {
				ContributionTreeNode newChild = createNode(childProto, fn);
				if (newChild == null)
					continue;
				pair.getSecond().getChildren().add(newChild);
				Pair<ProtoNode, ContributionTreeNode> nextPair = new Pair<>(
						childProto, newChild);
				queue.add(nextPair);
			}
		}
		return tree;
	}

	private ContributionTreeNode createNode(ProtoNode protoNode,
			ResultFetch fetch) {
		double amount = protoNode.share
				* fetch.getTotalAmount(protoNode.product);
		if (amount == 0 && skipNulls)
			return null;
		if (amount < 0 && skipNegatives)
			return null;
		ContributionTreeNode node = new ContributionTreeNode();
		node.setAmount(amount);
		node.setProcessProduct(protoNode.product);
		return node;
	}

	private interface ResultFetch {

		double getTotalAmount(LongPair product);

		BaseDescriptor getReference();
	}

	private class FlowResultFetch implements ResultFetch {

		private final FlowDescriptor flow;
		private final long flowId;

		public FlowResultFetch(FlowDescriptor flow) {
			this.flow = flow;
			this.flowId = flow.getId();
		}

		@Override
		public double getTotalAmount(LongPair product) {
			return result.getTotalFlowResult(product, flowId);
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
			return result.getTotalImpactResult(processProduct, impactId);
		}

		@Override
		public BaseDescriptor getReference() {
			return impact;
		}
	}

	private class ProtoNode {

		private final double share;
		private final LongPair product;
		private final ArrayList<ProtoNode> childs = new ArrayList<>();

		public ProtoNode(LongPair product, double share) {
			this.product = product;
			this.share = share;
		}

		@Override
		public int hashCode() {
			return product.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!getClass().equals(obj.getClass()))
				return false;
			ProtoNode other = (ProtoNode) obj;
			return Objects.equals(this.product, other.product);
		}
	}

}
