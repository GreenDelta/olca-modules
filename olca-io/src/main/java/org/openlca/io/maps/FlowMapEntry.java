package org.openlca.io.maps;

public class FlowMapEntry {

	private String openlcaFlowKey;
	private String externalFlowKey;
	private double conversionFactor = 1d;

	public String getOpenlcaFlowKey() {
		return openlcaFlowKey;
	}

	public void setOpenlcaFlowKey(String openlcaFlowKey) {
		this.openlcaFlowKey = openlcaFlowKey;
	}

	public String getExternalFlowKey() {
		return externalFlowKey;
	}

	public void setExternalFlowKey(String externalFlowKey) {
		this.externalFlowKey = externalFlowKey;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	@Override
	public String toString() {
		return "FlowMapEntry [openlcaFlowKey=" + openlcaFlowKey
				+ ", externalFlowKey=" + externalFlowKey
				+ ", conversionFactor=" + conversionFactor + "]";
	}

}
