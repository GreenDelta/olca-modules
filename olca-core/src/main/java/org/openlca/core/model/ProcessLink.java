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

	@Column(name = "f_provider_output")
	private long providerOutput;

	@Column(name = "f_provider_process")
	private long providerProcess;

	@Column(name = "f_recipient_input")
	private long recipientInput;

	@Column(name = "f_recipient_process")
	private long recipientProcess;

	public ProcessLink() {
	}

	@Override
	public ProcessLink clone() {
		final ProcessLink processLink = new ProcessLink();
		processLink.setProviderOutput(getProviderOutput());
		processLink.setProviderProcess(getProviderProcess());
		processLink.setRecipientInput(getRecipientInput());
		processLink.setRecipientProcess(getRecipientProcess());
		return processLink;
	}

	public long getProviderOutput() {
		return providerOutput;
	}

	public long getProviderProcess() {
		return providerProcess;
	}

	public long getRecipientInput() {
		return recipientInput;
	}

	public long getRecipientProcess() {
		return recipientProcess;
	}

	public void setProviderOutput(long providerOutput) {
		this.providerOutput = providerOutput;
	}

	public void setProviderProcess(long providerProcess) {
		this.providerProcess = providerProcess;
	}

	public void setRecipientInput(long recipientInput) {
		this.recipientInput = recipientInput;
	}

	public void setRecipientProcess(long recipientProcess) {
		this.recipientProcess = recipientProcess;
	}

	@Override
	public String toString() {
		return "ProcessLink [providerOutput=" + providerOutput
				+ ", providerProcess=" + providerProcess + ", recipientInput="
				+ recipientInput + ", recipientProcess=" + recipientProcess
				+ "]";
	}

}
