package org.openlca.core.results;

import org.openlca.core.model.descriptors.BaseDescriptor;

/**
 * Maps the upstream results of the product system graph to a tree where the
 * root is the reference process of the product system.
 */
public class UpstreamTree {

	private UpstreamTreeNode root;
	private BaseDescriptor reference;

	public UpstreamTreeNode getRoot() {
		return root;
	}

	public void setRoot(UpstreamTreeNode root) {
		this.root = root;
	}

	public BaseDescriptor getReference() {
		return reference;
	}

	public void setReference(BaseDescriptor reference) {
		this.reference = reference;
	}

}
