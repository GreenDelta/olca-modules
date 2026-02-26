package org.openlca.sd;

import java.io.File;

import org.openlca.sd.eqn.Simulator;
import org.openlca.sd.util.TensorPrinter;
import org.openlca.sd.xmile.Xmile;

public class FullExample {

	public static void main(String[] args) {
		var xmile = Xmile
			.readFrom(new File("examples/treasource-model.stmx"))
			.orElseThrow();
		var sim = Simulator.of(xmile).orElseThrow();

		var prnt = new TensorPrinter();
		var interpreter = sim.iterator().interpreter();

		var res = interpreter.eval("\"collection_for_recycling/biorecovery\"").orElseThrow();
		prnt.print(res);

/*
		res = interpreter.eval(res).orElseThrow();
		prnt.print(res);


		interpreter.context()
			.getVar(Id.of("\"collection_for_recycling/biorecovery\""))
			.orElseThrow()
			.pushValue(res);

		var cell = interpreter.eval("""
			 "collection_for_recycling/biorecovery"/(
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_1] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_2] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_3] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_4] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_5] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_6] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_7] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_1] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_2] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_3] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_4] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_5] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_6] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_recycling_7] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Byproduct_1] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Byproduct_2] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Byproduct_3] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Byproduct_4] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Byproduct_w_recycling_1] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_1] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_2] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_3] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_4] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_5] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_6] +
			      "collection_for_recycling/biorecovery"[EoL_Class,Product_w_reuse_7]
			      )

			""");
		prnt.print(cell.value());



		sim.forEach(r -> {
			if (r.hasError()) {
				System.out.println("error: " + r.error());
			}
		});
*/

	}
}
