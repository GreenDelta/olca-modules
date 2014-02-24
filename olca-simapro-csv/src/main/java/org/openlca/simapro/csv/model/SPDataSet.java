package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public abstract class SPDataSet {

	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<>();
	private List<SPInputParameter> inputParameters = new ArrayList<>();
	private List<SPElementaryFlow> elementaryFlows = new ArrayList<>();
	private List<SPProductFlow> productFlows = new ArrayList<>();
	private SPDocumentation documentation;
	private String subCategory;

	public List<SPCalculatedParameter> getCalculatedParameters() {
		return calculatedParameters;
	}

	public SPDocumentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(SPDocumentation documentation) {
		this.documentation = documentation;
	}

	public List<SPElementaryFlow> getElementaryFlows() {
		return elementaryFlows;
	}

	public List<SPElementaryFlow> getElementaryFlows(ElementaryFlowType type) {
		List<SPElementaryFlow> list = new ArrayList<SPElementaryFlow>();
		for (SPElementaryFlow flow : this.elementaryFlows) {
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
