package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.ProductFlowType;


/**
 * This class represents a SimaPro data entry (can be {@link SPProcess} or
 * {@link SPWasteTreatment}
 * 
 * @author Sebastian Greve
 * @author Imo Graf
 * 
 */
public abstract class SPDataEntry {

	/**
	 * The calculated parameters of the process
	 */
	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<SPCalculatedParameter>();

	/**
	 * The input parameters of the process
	 */
	private List<SPInputParameter> inputParameters = new ArrayList<SPInputParameter>();

	/**
	 * The documentation of the process
	 */
	private SPDocumentation documentation;

	/**
	 * The elementary flows of the process
	 */
	private List<SPElementaryFlow> elementaryFlows = new ArrayList<SPElementaryFlow>();

	/**
	 * The product flows of the process
	 */
	private List<SPProductFlow> productFlows = new ArrayList<SPProductFlow>();

	/**
	 * The known outputs to technosphere, Waste and emissions to treatment
	 */
	private List<SPWasteToTreatmentFlow> wasteToTreatmentFlows = new ArrayList<SPWasteToTreatmentFlow>();

	/**
	 * The sub category of the process
	 */
	private String subCategory;

	/**
	 * Adds a calculated parameter to the process
	 * 
	 * @param parameter
	 *            The parameter to add
	 */
	public void add(SPCalculatedParameter parameter) {
		calculatedParameters.add(parameter);
	}

	/**
	 * Adds an input parameter to the process
	 * 
	 * @param parameter
	 *            The parameter to add
	 */
	public void add(SPInputParameter parameter) {
		inputParameters.add(parameter);
	}

	/**
	 * Adds an elementary flow to the process
	 * 
	 * @param flow
	 *            The flow to add
	 */
	public void add(SPElementaryFlow flow) {
		elementaryFlows.add(flow);
	}

	/**
	 * Adds a product flow to the process
	 * 
	 * @param flow
	 *            The flow to add
	 */
	public void add(SPProductFlow flow) {
		productFlows.add(flow);
	}

	/**
	 * Adds a waste to treatment flow to the process
	 * 
	 * @param flow
	 *            The flow to add
	 */
	public void add(SPWasteToTreatmentFlow flow) {
		wasteToTreatmentFlows.add(flow);
	}

	/**
	 * Getter of the calculated parameters
	 * 
	 * @return The calculated parameters of the entry
	 */
	public SPCalculatedParameter[] getCalculatedParameters() {
		return calculatedParameters
				.toArray(new SPCalculatedParameter[calculatedParameters.size()]);
	}

	/**
	 * Getter of the documentation
	 * 
	 * @return The documentation of the data entry
	 */
	public SPDocumentation getDocumentation() {
		return documentation;
	}

	/**
	 * Getter of the elementary flows
	 * 
	 * @param type
	 *            The type of requested elementary flow
	 * @return The elementary flows of the entry matching the given type
	 */
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

	/**
	 * Getter of the input parameters
	 * 
	 * @return The input parameters of the entry
	 */
	public SPInputParameter[] getInputParameters() {
		return inputParameters.toArray(new SPInputParameter[inputParameters
				.size()]);
	}

	/**
	 * Getter of the product flows
	 * 
	 * @param type
	 *            The type of requested product flow
	 * @return The product flows of the entry matching the given type
	 */
	public SPProductFlow[] getProductFlows(ProductFlowType type) {
		List<SPProductFlow> productFlows = new ArrayList<SPProductFlow>();
		for (SPProductFlow flow : this.productFlows) {
			if (flow.getType() == type) {
				productFlows.add(flow);
			}
		}
		return productFlows.toArray(new SPProductFlow[productFlows.size()]);
	}

	/**
	 * Getter of the sub category
	 * 
	 * @return The sub category of the data entry
	 */
	public String getSubCategory() {
		return subCategory;
	}

	/**
	 * Setter of the documentation
	 * 
	 * @param documentation
	 *            The new documentation
	 */
	public void setDocumentation(SPDocumentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * Setter of the sub category
	 * 
	 * @param subCategory
	 *            The new sub category
	 */
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

	public SPWasteToTreatmentFlow[] getWasteToTreatmentFlows() {
		return wasteToTreatmentFlows
				.toArray(new SPWasteToTreatmentFlow[wasteToTreatmentFlows
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
