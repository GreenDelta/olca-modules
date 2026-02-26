package org.openlca.sd;

import java.io.File;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.eqn.SimulationState;
import org.openlca.sd.eqn.Simulator;
import org.openlca.sd.util.TensorPrinter;
import org.openlca.sd.xmile.Xmile;

public class SimulatorArrayExample {

	public static void main(String[] args) {
		var xmile = Xmile
				.readFrom(new File("examples/plastic-subs.stmx"))
				.orElseThrow();
		var sim = Simulator.of(xmile).orElseThrow();
		var iter = sim.iterator();

		Iterable<Res<SimulationState>> wrap = () -> iter;
		var stock = Id.of("plastic use");
		var printer = new TensorPrinter();
		for (var res : wrap) {
			if (res.isError()) {
				System.out.println("Error: " + res.error());
				var pop = iter.interpreter().context().getVar(stock).orElseThrow();
				printer.print(pop.value());
				break;
			}
			var state = res.value();
			printer.print(state.valueOf(stock).orElseThrow());
		}
	}
}
