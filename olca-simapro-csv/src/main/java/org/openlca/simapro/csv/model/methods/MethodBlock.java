package org.openlca.simapro.csv.model.methods;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionValue;

@BlockModel("Method")
public class MethodBlock {

	@SectionValue("Name")
	public String name;

	@SectionValue("Version")
	public String version;

	@SectionValue("Comment")
	public String comment;

	@SectionValue("Category")
	public String category;

	@SectionValue("Use Damage Assessment")
	public boolean useDamageAssessment;

	@SectionValue("Use Normalization")
	public boolean useNormalization;

	@SectionValue("Use Weighting")
	public boolean useWeighting;

	@SectionValue("Use Addition")
	public boolean useAddition;

	@SectionValue("Weighting unit")
	public String weightingUnit;

}
