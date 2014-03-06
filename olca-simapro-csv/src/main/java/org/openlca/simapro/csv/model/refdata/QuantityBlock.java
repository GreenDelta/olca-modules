package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;

@BlockModel("Quantities")
public class QuantityBlock {

	@BlockRows
	private List<Quantity> quantities = new ArrayList<>();

	public List<Quantity> getQuantities() {
		return quantities;
	}

}
