package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.util.Strings;

public class ProcessDescriptor extends BaseDescriptor {

	private static final long serialVersionUID = 5631406720764078522L;

	private ProcessType processType;
	private boolean infrastructureProcess;
	private String locationCode;

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

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	@Override
	public String getDisplayName() {
		String name = getName();
		String disp = name == null ? "no name" : Strings.cut(name, 75);
		if (locationCode != null)
			disp = disp.concat(" (").concat(locationCode).concat(")");
		return disp;
	}

}
