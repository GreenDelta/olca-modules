package org.openlca.sd;

import java.io.File;

import org.openlca.sd.eqn.EvaluationOrder;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.xmile.Xmile;

public class VarsGraph {

	public static void main(String[] args) {
		var xmile = Xmile
			.readFrom(new File("examples/treasource-model.stmx"))
			.orElseThrow();
		var vars = SdModel
			.readFrom(xmile)
			.orElseThrow()
			.vars();

		System.out.println("digraph g {");
		for (var v : vars) {
			for (var dep : EvaluationOrder.dependenciesOf(v)) {
				System.out.printf("  \"%s\" -> \"%s\";%n", dep.value(), v.name().value());
			}
		}
		System.out.println("}");

	}

}
