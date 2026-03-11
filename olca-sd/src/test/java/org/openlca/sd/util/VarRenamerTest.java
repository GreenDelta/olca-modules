package org.openlca.sd.util;

import org.junit.Test;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Rate;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.Stock;
import org.openlca.sd.model.cells.Cell;

import java.util.List;

public class VarRenamerTest {

	@Test
	public void testRename() {
		var flow = new Rate(Id.of("a flow"), Cell.of(42), "");
		var aux = new Auxil(Id.of("Aux"), Cell.of("2 * \"a Flow\""), "");
		var stock = new Stock(
			Id.of("Stock"), Cell.of(""), "", List.of(Id.of("A FLOW")), List.of());

		var model = new SdModel();
		model.vars().addAll(List.of(flow, aux, stock));

		VarRenamer.rename(model, flow, Id.of("Ein Fluss")).orElseThrow();


	}
}
