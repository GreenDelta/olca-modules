package org.openlca.sd;

import java.io.File;

import org.openlca.sd.eqn.EvaluationOrder;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.xmile.Xmile;

public class EvaluationOrderExample {

	public static void main(String[] args) {
		var xmile = Xmile
			.readFrom(new File("examples/treasource-model.stmx"))
			.orElseThrow();
		var vars = SdModel.readFrom(xmile)
			.orElseThrow()
			.vars();

		var order = EvaluationOrder.of(vars).orElseThrow();
		for (var v : order) {
			System.out.println(v.name().label());
		}
	}
}
