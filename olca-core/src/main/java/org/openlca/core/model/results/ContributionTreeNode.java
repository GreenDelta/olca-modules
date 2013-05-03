package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ContributionTreeNode {

	private ProcessDescriptor process;
	private double amount;
	private List<ContributionTreeNode> children = new ArrayList<>();

	public ProcessDescriptor getProcess() {
		return process;
	}

	public void setProcess(ProcessDescriptor process) {
		this.process = process;
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

}
