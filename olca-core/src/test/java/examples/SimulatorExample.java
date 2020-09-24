package examples;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;

public class SimulatorExample {

	public static void main(String[] args) {
		String workspace = "C:/Users/ms/openLCA-data-1.4";
		String dbPath = workspace
				+ "/databases/ecoinvent_2_2_unit";
		IDatabase db = new DerbyDatabase(new File(dbPath));
		ProductSystem system = new ProductSystemDao(db).getForRefId(
				"53f9b9db-139f-4617-bf2b-8fc715b3cd16");
		CalculationSetup setup = new CalculationSetup(system);
		setup.withUncertainties = true;

		ImpactMethodDao idao = new ImpactMethodDao(db);
		ImpactMethodDescriptor method = idao.getDescriptorForRefId(
				"207ffac9-aaa8-401d-ac90-874defd3751a");
		setup.impactMethod = method;
		ImpactDescriptor gwp = null;
		for (ImpactDescriptor i : idao
				.getCategoryDescriptors(method.id)) {
			if (i.name.equals("Climate change - GWP100")) {
				gwp = i;
				break;
			}
		}
		System.out.println("Tacking results of " + gwp.name);

		String juliaLibPath = "C:/Users/ms/Projects/openLCA/eclipse";
		Julia.loadFromDir(new File(juliaLibPath));
		JuliaSolver solver = new JuliaSolver();

		Simulator simulator = Simulator.create(setup, db, solver);

		double min = 0;
		double max = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < 200; i++) {
			SimpleResult r = simulator.nextRun();
			double val = r.getTotalImpactResult(gwp);
			if (i == 0) {
				min = val;
				max = val;
			} else {
				min = Math.min(min, val);
				max = Math.max(max, val);
			}
			if ((i + 1) % 20 == 0) {
				double t = (System.currentTimeMillis() - start) / 1000.0;
				System.out.printf(
						"after %d iterations min=%.2f max=%.2f t=%.2f\n",
						i + 1, min, max, t);
			}
		}
		System.out.println("all done");
	}

}
