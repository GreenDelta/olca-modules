package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;

@BlockModel("Quantities")
public class QuantityBlock {

	@BlockRows
	private List<QuantityRow> quantities = new ArrayList<>();

	public List<QuantityRow> getQuantities() {
		return quantities;
	}

}
