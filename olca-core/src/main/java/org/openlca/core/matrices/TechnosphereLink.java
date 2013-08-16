package org.openlca.core.matrices;

/**
 * Describes an input or output of a product or waste flow of a process (a link
 * of a process to the technosphere). An instance of this class holds only the
 * minimal information that describes such an exchange and is intended to build
 * product system graphs in an efficient way.
 */
public class TechnosphereLink {

	private long processId;
	private long flowId;
	private double amount;
	private boolean input;
	private boolean waste;
	private long defaultProviderId;

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	public boolean isWaste() {
		return waste;
	}

	public void setWaste(boolean waste) {
		this.waste = waste;
	}

	public long getDefaultProviderId() {
		return defaultProviderId;
	}

	public void setDefaultProviderId(long defaultProviderId) {
		this.defaultProviderId = defaultProviderId;
	}

}
