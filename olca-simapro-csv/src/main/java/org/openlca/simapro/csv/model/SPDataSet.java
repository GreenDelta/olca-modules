package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public abstract class SPDataSet {

	private List<CalculatedParameterRow> calculatedParameters = new ArrayList<>();
	private List<InputParameterRow> inputParameters = new ArrayList<>();
	private List<ElementaryExchangeRow> elementaryFlows = new ArrayList<>();
	private List<ProductInputRow> productFlows = new ArrayList<>();
	private SPProcessDocumentation documentation;
	private String subCategory;

	public List<CalculatedParameterRow> getCalculatedParameters() {
		return calculatedParameters;
	}

	public SPProcessDocumentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(SPProcessDocumentation documentation) {
		this.documentation = documentation;
	}

	public List<ElementaryExchangeRow> getElementaryFlows() {
		return elementaryFlows;
	}

	public List<ElementaryExchangeRow> getElementaryFlows(ElementaryFlowType type) {
		List<ElementaryExchangeRow> list = new ArrayList<ElementaryExchangeRow>();
		for (ElementaryExchangeRow flow : this.elementaryFlows) {
			if (flow.getType() == type)
				list.add(flow);
		}
		return list;
	}

	public List<InputParameterRow> getInputParameters() {
		return inputParameters;
	}

	public List<ProductInputRow> getProductFlows() {
		return productFlows;
	}

	public List<ProductInputRow> getProductFlows(ProductFlowType type) {
		List<ProductInputRow> list = new ArrayList<ProductInputRow>();
		for (ProductInputRow flow : this.productFlows) {
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
