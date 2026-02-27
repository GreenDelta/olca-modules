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
import org.openlca.sd.model.VarBinding;
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
		for (var simState : simulator) {
			if (progress.isCanceled())
				break;
			if (simState.isError()) {
				error = simState.wrapError("Simulation error");
				break;
			}
			var lcaResults = calculateSystems(simState.value(), progress);
			if (error != null || progress.isCanceled())
				break;
			result.append(simState.value(), lcaResults);
			progress.worked(1);
		}
	}

	private List<LcaResult> calculateSystems(
			SimulationState state, Progress progress) {
		var results = new ArrayList<LcaResult>();
		for (var binding : model.lca().systemBindings()) {
			if (progress.isCanceled())
				break;
			var lcaResult = calculateSystem(state, binding);
			if (error != null)
				break;
			results.add(lcaResult);
		}
		return results;
	}

	private LcaResult calculateSystem(
			SimulationState state, SystemBinding binding) {
		var params = bindParameterValues(state, binding);
		if (params.isError()) {
			error = params.wrapError("Variable binding error");
			return null;
		}
		var setup = setupOf(binding, params.value());
		try {
			return calculator.calculate(setup);
		} catch (Exception e) {
			error = Res.error(
					"Calculation failed: " + binding.system().name(), e);
			return null;
		}
	}

	private CalculationSetup setupOf(
			SystemBinding binding, List<ParameterRedef> params) {
		var setup = CalculationSetup.of(resolver.systemOf(binding))
				.withParameters(params)
				.withAllocation(binding.allocation())
				.withAmount(binding.amount());
		var method = resolver.impactMethod();
		if (method != null) {
			setup = setup.withImpactMethod(method);
		}
		return setup;
	}

	private Res<List<ParameterRedef>> bindParameterValues(
			SimulationState state, SystemBinding binding) {
		var templates = resolver.paramsOf(binding);
		var params = new ArrayList<ParameterRedef>(templates.size());
		int i = 0;
		for (var vb : binding.varBindings()) {
			if (vb.varId() == null || vb.parameter() == null)
				continue;
			var value = resolveVarValue(state, vb);
			if (value.isError())
				return value.castError();
			var param = templates.get(i).copy();
			param.value = value.value();
			params.add(param);
			i++;
		}
		return Res.ok(params);
	}

	private Res<Double> resolveVarValue(
			SimulationState state, VarBinding vb) {
		var cell = state.valueOf(vb.varId()).orElse(null);
		if (cell == null)
			return Res.error("Variable not found: " + vb.varId());
		if (!(cell instanceof NumCell(double num)))
			return Res.error("Variable is not a number: " + vb.varId());
		return Res.ok(num);
	}
}
