package org.openlca.simapro.csv.model.process;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionRows;
import org.openlca.simapro.csv.model.annotations.SectionValue;
import org.openlca.simapro.csv.model.enums.ProcessCategory;

@BlockModel("Process")
public class ProcessBlock {

	@SectionValue("Category type")
	private ProcessCategory category;

	@SectionRows("Products")
	private List<ProductOutputRow> products = new ArrayList<>();

	public List<ProductOutputRow> getProducts() {
		return products;
	}

	public ProcessCategory getCategory() {
		return category;
	}

	public void setCategory(ProcessCategory category) {
		this.category = category;
	}
}
