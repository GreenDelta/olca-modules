package org.openlca.core.model.results;

import org.openlca.core.model.descriptors.BaseDescriptor;

public class ContributionTree {

	private ContributionTreeNode root;
	private BaseDescriptor reference;

	public ContributionTreeNode getRoot() {
		return root;
	}

	public void setRoot(ContributionTreeNode root) {
		this.root = root;
	}

	public BaseDescriptor getReference() {
		return reference;
	}

	public void setReference(BaseDescriptor reference) {
		this.reference = reference;
	}

}
