package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionValue;

@BlockModel("System description")
public class SystemDescriptionBlock {

	@SectionValue("Name")
	public String name;

	@SectionValue("Category")
	public String category;

	@SectionValue("Description")
	public String description;

	@SectionValue("Sub-systems")
	public String subSystems;

	@SectionValue("Cut-off rules")
	public String cutOffRules;

	@SectionValue("Energy model")
	public String energyModel;

	@SectionValue("Transport model")
	public String transportModel;

	@SectionValue("Waste model")
	public String wasteModel;

	@SectionValue("Other assumptions")
	public String otherAssumptions;

	@SectionValue("Other information")
	public String otherInformation;

	@SectionValue("Allocation rules")
	public String allocationRules;

}
