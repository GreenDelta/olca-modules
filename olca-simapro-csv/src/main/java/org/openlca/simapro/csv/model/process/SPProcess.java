package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionRows;

import java.util.ArrayList;
import java.util.List;

@BlockModel("Process")
public class SPProcess {

	@SectionRows("Products")
	private List<SPProductOutput> products = new ArrayList<>();

	public List<SPProductOutput> getProducts() {
		return products;
	}
}
