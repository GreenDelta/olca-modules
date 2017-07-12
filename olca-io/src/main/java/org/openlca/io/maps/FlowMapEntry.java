package org.openlca.io.maps;

public class FlowMapEntry {

	public String openlcaFlowKey;
	public String externalFlowKey;
	public double conversionFactor = 1d;

	@Override
	public String toString() {
		return "FlowMapEntry [openlcaFlowKey=" + openlcaFlowKey
				+ ", externalFlowKey=" + externalFlowKey
				+ ", conversionFactor=" + conversionFactor + "]";
	}

}
