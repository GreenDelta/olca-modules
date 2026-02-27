package org.openlca.sd.interop;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.results.LcaResult;
import org.openlca.sd.eqn.SimulationState;
import org.openlca.sd.eqn.Simulator;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.SystemBinding;
import org.openlca.sd.model.cells.NumCell;

public class CoupledSimulator {

	private final Simulator simulator;
	private final SdModel model;
	private final Resolver resolver;
	private final SystemCalculator calculator;
	private final CoupledResult result;
	private Res<?> error;

	private CoupledSimulator(
			Simulator simulator,
			SdModel model,
			Resolver resolver,
			SystemCalculator calculator) {
		this.simulator = simulator;
		this.model = model;
		this.resolver = resolver;
		this.calculator = calculator;
		this.result = new CoupledResult();
	}

	public static Res<CoupledSimulator> of(
			SdModel model, IDatabase db, SystemCalculator calculator) {
		var sim = Simulator.of(model);
		if (sim.isError())
			return sim.wrapError("failed to create simulator");
		var resolver = Resolver.of(model, db);
		if (resolver.isError())
			return resolver.wrapError("failed to resolve model references");
		return Res.ok(new CoupledSimulator(
				sim.value(), model, resolver.value(), calculator));
	}

	public Res<CoupledResult> getResult() {
		return error != null
				? error.castError()
				: Res.ok(result);
	}

	public interface Progress {
		void worked(int work);
		boolean isCanceled();
	}

	public void run(Progress progress) {
		for (var res : simulator) {
			if (progress.isCanceled())
				break;

			if (res.isError()) {
				error = res.wrapError("Simulation error");
				break;
			}

			var simState = res.value();
			var rs = new ArrayList<LcaResult>();
			for (var b : model.lca().systemBindings()) {
				if (progress.isCanceled())
					break;

				var params = paramsOf(simState, b);
				if (params.isError()) {
					error = params.wrapError("Variable binding error");
					break;
				}

				var calcSetup = CalculationSetup.of(resolver.systemOf(b))
						.withParameters(params.value())
						.withAllocation(b.allocation());
				var method = resolver.impactMethod();
				if (method != null) {
					calcSetup = calcSetup.withImpactMethod(method);
				}
				calcSetup = calcSetup.withAmount(b.amount());

				try {
					var lcaResult = calculator.calculate(calcSetup);
					rs.add(lcaResult);
				} catch (Exception e) {
					error = Res.error(
							"Calculation of system failed: "
									+ b.system().name(), e);
					break;
				}
			}

			if (error != null)
				break;

			result.append(simState, rs);
			progress.worked(1);
		}
	}

	private Res<List<ParameterRedef>> paramsOf(
			SimulationState simState, SystemBinding binding) {

		var templates = resolver.paramsOf(binding);
		var params = new ArrayList<ParameterRedef>(templates.size());

		int i = 0;
		for (var vb : binding.varBindings()) {
			if (vb.varId() == null || vb.parameter() == null)
				continue;
			var cell = simState.valueOf(vb.varId()).orElse(null);
			if (cell == null)
				return Res.error("Variable not found: " + vb.varId());
			if (!(cell instanceof NumCell(double num))) {
				return Res.error(
						"Variable does not evaluate to a number: " + vb.varId());
			}

			var param = templates.get(i).copy();
			param.value = num;
			params.add(param);
			i++;
		}
		return Res.ok(params);
	}
}
