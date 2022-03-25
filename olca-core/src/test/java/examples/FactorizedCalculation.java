package examples;

import java.io.File;

import org.openlca.core.library.Library;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.nativelib.NativeLib;

public class FactorizedCalculation {

	public static void main(String[] args) {

		// initialize the solver
		var libDir = "C:/Users/ms/Projects/openLCA/repos/olca-rust/bin";
		NativeLib.loadFrom(new File(libDir));
		var solver = MatrixSolver.get();

		var libPath = "C:/Users/ms/openLCA-data-1.4/libraries/" +
				"ecoinvent_apos_37_up_20200908_1__00.00.001";
		var lib = new Library(new File(libPath));

		// find the reference product
		var processID = "3ebe93ff-793b-3c60-bd23-bad14658f8ca";
		var products = lib.getProductIndex()
				.getProductList();
		var refProduct = products.stream()
				.filter(p -> p.getProcess().getId().equals(processID))
				.findAny()
				.orElseThrow();

		// create the demand vector
		var demand = new double[products.size()];
		demand[refProduct.getIndex()] = 1.0;

		// calculate the scaling vector
		var techMatrix = lib.getMatrix(LibMatrix.A)
				.orElseThrow();
		var factorization = solver.factorize(techMatrix);
		var scaling = factorization.solve(demand);

		// calculate the LCI result
		var enviMatrix = lib.getMatrix(LibMatrix.B)
				.orElseThrow();
		var inventory = solver.multiply(enviMatrix, scaling);

		var elemFlows = lib.getElemFlowIndex().getFlowList();
		for (var elemFlow : elemFlows) {
			var value = inventory[elemFlow.getIndex()];
			if (value == 0)
				continue;
			var flow = elemFlow.getFlow();
			System.out.format("%s\t%s\t%s\t%s%n",
					flow.getName(),
					flow.getCategory(),
					value,
					flow.getUnit());
		}

		factorization.dispose();
	}

}
