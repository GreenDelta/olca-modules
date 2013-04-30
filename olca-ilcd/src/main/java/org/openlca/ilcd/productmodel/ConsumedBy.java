package org.openlca.ilcd.productmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConsumedBy {

	@XmlAttribute
	protected String processId;

	@XmlAttribute
	protected String flowUUID;

	@XmlAttribute
	protected Double amount;

	public ConsumedBy() {
	}

	public ConsumedBy(String processId, String flowId) {
		this(processId, flowId, null);
	}

	public ConsumedBy(String processId, String flowUUID, Double amount) {
		this.processId = processId;
		this.flowUUID = flowUUID;
		this.amount = amount;
	}

	/**
	 * @return the processId
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * @param processId
	 *            the processId to set
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	/**
	 * @return the flowId
	 */
	public String getFlowUUID() {
		return flowUUID;
	}

	/**
	 * @param flowId
	 *            the flowId to set
	 */
	public void setFlowUUID(String flowUUID) {
		this.flowUUID = flowUUID;
	}

	/**
	 * @return the amount
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}
}
