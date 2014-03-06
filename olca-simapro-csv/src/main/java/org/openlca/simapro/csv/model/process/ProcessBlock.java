package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionRows;

import java.util.ArrayList;
import java.util.List;

@BlockModel("Process")
public class ProcessBlock {

	@SectionRows("Products")
	private List<ProductOutputRow> products = new ArrayList<>();

	public List<ProductOutputRow> getProducts() {
		return products;
	}
}
