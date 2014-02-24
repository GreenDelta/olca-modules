package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro waste treatment
 */
public class SPWasteTreatment extends SPDataSet {

	private SPWasteSpecification wasteSpecification;
	
	public SPWasteTreatment(SPWasteSpecification wasteSpecification) {
		this.wasteSpecification = wasteSpecification;
	}

	public SPWasteTreatment(SPWasteSpecification wasteSpecification,
			String subCategory, SPProcessDocumentation documentation) {
		this.wasteSpecification = wasteSpecification;
		setSubCategory(subCategory);
		setDocumentation(documentation);
	}

	public SPWasteSpecification getWasteSpecification() {
		return wasteSpecification;
	}

	public void setWasteSpecification(SPWasteSpecification wasteSpecification) {
		this.wasteSpecification = wasteSpecification;
	}

}
