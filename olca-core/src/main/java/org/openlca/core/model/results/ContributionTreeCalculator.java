package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Calculates a contribution tree of the processes in a product system to a flow
 * or impact assessment result.
 */
public class ContributionTreeCalculator {

	private AnalysisResult result;
	private LinkContributions linkContributions;
	private boolean skipNegatives = false;
	private boolean skipNulls = false;
	private ProductSystem system;

	public ContributionTreeCalculator(AnalysisResult result,
			LinkContributions linkContributions) {
		this.result = result;
		this.system = result.getSetup().getProductSystem();
		this.linkContributions = linkContributions;
	}

	public void skipNegativeValues(boolean skipNegatives) {
		this.skipNegatives = skipNegatives;
	}

	public void skipNullValues(boolean skipNulls) {
		this.skipNulls = skipNulls;
	}

	public ContributionTree calculate(Flow flow) {
		FlowResultFetch fn = new FlowResultFetch(flow);
		return calculate(fn);
	}

	public ContributionTree calculate(ImpactCategoryDescriptor impact) {
		ImpactResultFetch fn = new ImpactResultFetch(impact);
		return calculate(fn);
	}

	private ContributionTree calculate(ResultFetch fn) {
		Process refProcess = system.getReferenceProcess();
		double totalAmount = fn.getAmount(refProcess);
		ContributionTreeNode root = createNode(refProcess, totalAmount);
		fillBreadthFirst(root, fn);
		ContributionTree tree = new ContributionTree();
		tree.setRoot(root);
		tree.setReference(fn.getReference());
		return tree;
	}

	private void fillBreadthFirst(ContributionTreeNode root, ResultFetch fn) {
		Queue<ContributionTreeNode> queue = new LinkedList<>();
		queue.add(root);
		List<ContributionTreeNode> handled = new ArrayList<>();
		while (!queue.isEmpty()) {
			ContributionTreeNode node = queue.poll();
			handled.add(node);
			for (ProcessLink link : system.getIncomingLinks(node.getProcess()
					.getRefId())) {
				Process provider = link.getProviderProcess();
				double rawAmount = fn.getAmount(provider);
				double factor = linkContributions.getShare(link);
				double amount = factor * rawAmount;
				if ((skipNulls && amount == 0) || (skipNegatives && amount < 0))
					continue;
				ContributionTreeNode child = createNode(provider, amount);
				node.getChildren().add(child);
				if (!containsProcess(queue, child)
						&& !containsProcess(handled, child))
					queue.add(child);
			}
		}
	}

	private ContributionTreeNode createNode(Process process, double amount) {
		ContributionTreeNode node = new ContributionTreeNode();
		node.setAmount(amount);
		ProcessDescriptor p = Descriptors.toDescriptor(process);
		node.setProcess(p);
		return node;
	}

	private boolean containsProcess(Collection<ContributionTreeNode> nodes,
			ContributionTreeNode node) {
		for (ContributionTreeNode collNode : nodes) {
			if (node.getProcess().equals(collNode.getProcess()))
				return true;
		}
		return false;
	}

	private interface ResultFetch {
		double getAmount(Process p);

		BaseDescriptor getReference();
	}

	private class FlowResultFetch implements ResultFetch {

		private Flow flow;

		public FlowResultFetch(Flow flow) {
			this.flow = flow;
		}

		@Override
		public double getAmount(Process p) {
			return result.getResult(p, flow);
		}

		@Override
		public BaseDescriptor getReference() {
			return Descriptors.toDescriptor(flow);
		}
	}

	private class ImpactResultFetch implements ResultFetch {
		private ImpactCategoryDescriptor impact;

		public ImpactResultFetch(ImpactCategoryDescriptor impact) {
			this.impact = impact;
		}

		@Override
		public double getAmount(Process p) {
			return result.getResult(p, impact);
		}

		@Override
		public BaseDescriptor getReference() {
			return impact;
		}
	}

}
