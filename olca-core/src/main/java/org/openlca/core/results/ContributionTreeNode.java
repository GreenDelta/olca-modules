package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.LongPair;

public class ContributionTreeNode {

	private LongPair processProduct;
	private double amount;
	private double share;
	private List<ContributionTreeNode> children = new ArrayList<>();

	public LongPair getProcessProduct() {
		return processProduct;
	}

	public void setProcessProduct(LongPair processProduct) {
		this.processProduct = processProduct;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public List<ContributionTreeNode> getChildren() {
		return children;
	}

	protected double getShare() {
		return share;
	}

	protected void setShare(double share) {
		this.share = share;
	}

}
