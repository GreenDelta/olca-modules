package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.ProductFlowType;

/**
 * This class represents a SimaPro data entry (can be {@link SPProcess} or
 * {@link SPWasteTreatment} <<<<<<< Updated upstream =======
 * 
 * 
 * >>>>>>> Stashed changes
 */
public abstract class SPDataEntry {

	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<SPCalculatedParameter>();
	private List<SPInputParameter> inputParameters = new ArrayList<SPInputParameter>();
	private List<SPElementaryFlow> elementaryFlows = new ArrayList<SPElementaryFlow>();
	private List<SPProductFlow> productFlows = new ArrayList<SPProductFlow>();

	private SPDocumentation documentation;
	private String subCategory;

	public void add(SPCalculatedParameter parameter) {
		calculatedParameters.add(parameter);
	}

	public void add(SPInputParameter parameter) {
		inputParameters.add(parameter);
	}

	public void add(SPElementaryFlow flow) {
		elementaryFlows.add(flow);
	}

	public void add(SPProductFlow flow) {
		productFlows.add(flow);
	}

	public SPCalculatedParameter[] getCalculatedParameters() {
		return calculatedParameters
				.toArray(new SPCalculatedParameter[calculatedParameters.size()]);
	}

	public SPDocumentation getDocumentation() {
		return documentation;
	}

	public SPElementaryFlow[] getElementaryFlows(ElementaryFlowType type) {
		List<SPElementaryFlow> elementaryFlows = new ArrayList<SPElementaryFlow>();
		for (SPElementaryFlow flow : this.elementaryFlows) {
			if (flow.getType() == type) {
				elementaryFlows.add(flow);
			}
		}
		return elementaryFlows.toArray(new SPElementaryFlow[elementaryFlows
				.size()]);
	}

	public SPInputParameter[] getInputParameters() {
		return inputParameters.toArray(new SPInputParameter[inputParameters
				.size()]);
	}

	public SPProductFlow[] getProductFlows(ProductFlowType type) {
		List<SPProductFlow> productFlows = new ArrayList<SPProductFlow>();
		for (SPProductFlow flow : this.productFlows) {
			if (flow.getType() == type) {
				productFlows.add(flow);
			}
		}
		return productFlows.toArray(new SPProductFlow[productFlows.size()]);
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setDocumentation(SPDocumentation documentation) {
		this.documentation = documentation;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public SPProductFlow[] getProductFlows() {
		return productFlows.toArray(new SPProductFlow[productFlows.size()]);
	}

	public SPElementaryFlow[] getElementaryFlows() {
		return elementaryFlows.toArray(new SPElementaryFlow[elementaryFlows
				.size()]);
	}

	public boolean containsInputParameter(String name) {
		boolean result = false;

		for (SPInputParameter parameter : inputParameters) {
			if (parameter.getName().equals(name)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
