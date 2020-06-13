package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionValue;

@BlockModel("Literature reference")
public class LiteratureReferenceBlock {

	@SectionValue("Name")
	public String name;

	@SectionValue("Documentation link")
	public String documentationLink;

	@SectionValue("Category")
	public String category;

	@SectionValue("Description")
	public String description;

	@Override
	public String toString() {
		return "SPLiteratureReference [name=" + name + "]";
	}

}
