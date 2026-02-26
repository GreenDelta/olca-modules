package org.openlca.sd;

import static org.openlca.sd.model.Subscript.*;

import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.eqn.func.Add;
import org.openlca.sd.util.TensorPrinter;

public class TensorExamples {

	public static void main(String[] args) {

		var printer = new TensorPrinter();

		var plastics = Dimension.of("Plastics", "PET", "PVC", "ABS");
		var metals = Dimension.of("Metals", "Fe", "Cu", "Al");

		var t1 = Tensor.of(plastics);
		printer.print(t1);

		var t2 = Tensor.of(plastics, metals);
		printer.print(t2);

		t2.set(0, Cell.of("X"));
		printer.print(t2);

		t2.set(Subscript.parseAllFrom("PET, *"), Cell.of(1));
		t2.set(Subscript.parseAllFrom("PVC, *"), Cell.of(2));
		t2.set(Subscript.parseAllFrom("ABS, *"), Cell.of(3));
		t2.set(Subscript.parseAllFrom("*, Cu"), Cell.of(5));
		printer.print(t2);

		printer.print(t2.get(of("*")));
		printer.print(t2.get(of("PET")));
		printer.print(t2.get(of("*"), of("Al")));

		printer.print(t2.get(of("Plastics"), of("Fe")));
		printer.print(Add.apply(t2.get(of("Plastics"), of("Fe")), t2.get(of("*"), of("Al"))).value());
	}

}
