package org.openlca.ilcd.util;

import org.openlca.ilcd.methods.DataSetInformation;
import org.openlca.ilcd.methods.LCIAMethod;

public class MethodBag implements IBag<LCIAMethod> {

	private LCIAMethod method;

	public MethodBag(LCIAMethod method) {
		this.method = method;
	}

	@Override
	public LCIAMethod getValue() {
		return method;
	}

	@Override
	public String getId() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getUUID();
		return null;
	}

	public String getImpactIndicator() {
		DataSetInformation info = getDataSetInformation();
		if (info != null)
			return info.getImpactIndicator();
		return null;
	}

	private DataSetInformation getDataSetInformation() {
		if (method.getLCIAMethodInformation() != null)
			return method.getLCIAMethodInformation().getDataSetInformation();
		return null;
	}

}
