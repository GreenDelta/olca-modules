package org.openlca.ilcd.util;

import org.openlca.ilcd.methods.DataSetInfo;
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
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.uuid;
		return null;
	}

	public String getImpactIndicator() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return info.indicator;
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (method.methodInfo != null)
			return method.methodInfo.dataSetInfo;
		return null;
	}

}
