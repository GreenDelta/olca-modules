package org.openlca.io.maps;

/**
 * Describes a mapping between a reference flow in the openLCA database and an
 * external flow (e.g. in an ILCD data set that is imported).
 */
public class FlowMapEntry {

	public String referenceFlowID;
	public String externalFlowID;
	public double conversionFactor = 1d;

	@Override
	public String toString() {
		return "FlowMapEntry [referenceFlowID=" + referenceFlowID
				+ ", externalFlowID=" + externalFlowID
				+ ", conversionFactor=" + conversionFactor + "]";
	}

}
