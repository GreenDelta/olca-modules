package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro waste treatment
 * 
 * @author Sebastian Greve
 * 
 */
public class SPWasteTreatment extends SPDataEntry {

	/**
	 * The waste specification of the waste treatment
	 */
	private SPWasteSpecification wasteSpecification;

	/**
	 * Creates a new waste treatment
	 * 
	 * @param wasteSpecification
	 *            The waste specification of the waste treatment
	 */
	public SPWasteTreatment(SPWasteSpecification wasteSpecification) {
		this.wasteSpecification = wasteSpecification;
	}

	/**
	 * Creates a new waste treatment
	 * 
	 * @param wasteSpecification
	 *            The waste specification of the waste treatment
	 * @param subCategory
	 *            The sub category of the waste treatment
	 * @param documentation
	 *            The documentation of the waste treatment
	 */
	public SPWasteTreatment(SPWasteSpecification wasteSpecification,
			String subCategory, SPDocumentation documentation) {
		this.wasteSpecification = wasteSpecification;
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	/**
	 * Getter of the waste specification
	 * 
	 * @see SPWasteSpecification
	 * @return The output of the waste treatment
	 */
	public SPWasteSpecification getWasteSpecification() {
		return wasteSpecification;
	}

	/**
	 * Setter of the waste specification
	 * 
	 * @param wasteSpecification
	 *            The new waste specification
	 */
	public void setWasteSpecification(SPWasteSpecification wasteSpecification) {
		this.wasteSpecification = wasteSpecification;
	}

}
