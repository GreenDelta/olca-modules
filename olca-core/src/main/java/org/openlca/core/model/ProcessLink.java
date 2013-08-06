/*******************************************************************************
 * Copyright (c) 2007 - 2013 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A process link is a link between a providing process and a receiving process.
 * The link is realized with a providing exchange and a receiving exchange,
 * which must have the same flow
 */
@Entity
@Table(name = "tbl_process_links")
public class ProcessLink extends AbstractEntity implements Cloneable {

	@Column(name = "f_flow")
	private long flowId;

	@Column(name = "f_provider_process")
	private long providerProcessId;

	@Column(name = "f_recipient_process")
	private long recipientProcessId;

	@Override
	public ProcessLink clone() {
		ProcessLink clone = new ProcessLink();
		clone.setFlowId(getFlowId());
		clone.setProviderProcessId(getProviderProcessId());
		clone.setRecipientProcessId(getRecipientProcessId());
		return clone;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public long getProviderProcessId() {
		return providerProcessId;
	}

	public void setProviderProcessId(long providerProcessId) {
		this.providerProcessId = providerProcessId;
	}

	public long getRecipientProcessId() {
		return recipientProcessId;
	}

	public void setRecipientProcessId(long recipientProcessId) {
		this.recipientProcessId = recipientProcessId;
	}

}
