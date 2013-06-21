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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A process link is a link between a providing process and a receiving process.
 * The link is realized with a providing exchange and a receiving exchange,
 * which must have the same flow
 */
@Entity
@Table(name = "tbl_process_links")
public class ProcessLink extends AbstractEntity implements Cloneable {

	@OneToOne
	@JoinColumn(name = "f_provider_output")
	private Exchange providerOutput;

	@OneToOne
	@JoinColumn(name = "f_provider_process")
	private Process providerProcess;

	@OneToOne
	@JoinColumn(name = "f_recipient_input")
	private Exchange recipientInput;

	@OneToOne
	@JoinColumn(name = "f_recipient_process")
	private Process recipientProcess;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public ProcessLink() {
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public ProcessLink clone() {
		final ProcessLink processLink = new ProcessLink();
		processLink.setId(UUID.randomUUID().toString());
		processLink.setProviderOutput(getProviderOutput());
		processLink.setProviderProcess(getProviderProcess());
		processLink.setRecipientInput(getRecipientInput());
		processLink.setRecipientProcess(getRecipientProcess());
		return processLink;
	}

	public Exchange getProviderOutput() {
		return providerOutput;
	}

	public Process getProviderProcess() {
		return providerProcess;
	}

	public Exchange getRecipientInput() {
		return recipientInput;
	}

	public Process getRecipientProcess() {
		return recipientProcess;
	}

	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setProviderOutput(final Exchange providerOutput) {
		support.firePropertyChange("providerOutput", this.providerOutput,
				this.providerOutput = providerOutput);
	}

	public void setProviderProcess(final Process providerProcess) {
		support.firePropertyChange("providerProcess", this.providerProcess,
				this.providerProcess = providerProcess);
	}

	public void setRecipientInput(final Exchange recipientInput) {
		support.firePropertyChange("recipientInput", this.recipientInput,
				this.recipientInput = recipientInput);
	}

	public void setRecipientProcess(final Process recipientProcess) {
		support.firePropertyChange("recipientProcess", this.recipientProcess,
				this.recipientProcess = recipientProcess);
	}

	@Override
	public String toString() {
		return "ProcessLink [providerOutput=" + providerOutput
				+ ", providerProcess=" + providerProcess + ", recipientInput="
				+ recipientInput + ", recipientProcess=" + recipientProcess
				+ "]";
	}

}
