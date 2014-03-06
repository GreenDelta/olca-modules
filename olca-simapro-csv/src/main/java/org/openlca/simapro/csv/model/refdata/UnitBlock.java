package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;

@BlockModel("Units")
public class UnitBlock {

	@BlockRows
	private List<UnitRow> units = new ArrayList<>();

	public List<UnitRow> getUnits() {
		return units;
	}

}
