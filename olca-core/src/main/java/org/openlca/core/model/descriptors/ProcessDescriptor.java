package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;

public class ProcessDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = 5631406720764078522L;

	private ProcessType processType;
	private boolean infrastructureProcess;
	private Long location;
	private Long quantitativeReference;

	public ProcessDescriptor() {
		setType(ModelType.PROCESS);
	}

	public ProcessType getProcessType() {
		return processType;
	}

	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}

	public boolean isInfrastructureProcess() {
		return infrastructureProcess;
	}

	public void setInfrastructureProcess(boolean infrastructureProcess) {
		this.infrastructureProcess = infrastructureProcess;
	}

	public Long getLocation() {
		return location;
	}

	public void setLocation(Long location) {
		this.location = location;
	}

	public Long getQuantitativeReference() {
		return quantitativeReference;
	}

	public void setQuantitativeReference(Long quantitativeReference) {
		this.quantitativeReference = quantitativeReference;
	}

}
