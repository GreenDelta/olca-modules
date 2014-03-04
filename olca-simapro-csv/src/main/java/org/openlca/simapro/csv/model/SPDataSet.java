package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public abstract class SPDataSet {

	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<>();
	private List<SPInputParameter> inputParameters = new ArrayList<>();
	private List<SPElementaryExchange> elementaryFlows = new ArrayList<>();
	private List<SPProductFlow> productFlows = new ArrayList<>();
	private SPProcessDocumentation documentation;
	private String subCategory;

	public List<SPCalculatedParameter> getCalculatedParameters() {
		return calculatedParameters;
	}

	public SPProcessDocumentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(SPProcessDocumentation documentation) {
		this.documentation = documentation;
	}

	public List<SPElementaryExchange> getElementaryFlows() {
		return elementaryFlows;
	}

	public List<SPElementaryExchange> getElementaryFlows(ElementaryFlowType type) {
		List<SPElementaryExchange> list = new ArrayList<SPElementaryExchange>();
		for (SPElementaryExchange flow : this.elementaryFlows) {
			if (flow.getType() == type)
				list.add(flow);
		}
		return list;
	}

	public List<SPInputParameter> getInputParameters() {
		return inputParameters;
	}

	public List<SPProductFlow> getProductFlows() {
		return productFlows;
	}

	public List<SPProductFlow> getProductFlows(ProductFlowType type) {
		List<SPProductFlow> list = new ArrayList<SPProductFlow>();
		for (SPProductFlow flow : this.productFlows) {
			if (flow.getType() == type)
				list.add(flow);
		}
		return list;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

}
