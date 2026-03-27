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
import org.openlca.sd.model.Id;
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
		this.result = new CoupledResult(model.simSpecs());
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

	public void run(Progress progress) {
		for (var simState : simulator) {
			if (progress.isCanceled()) break;
			if (simState.isError()) {
				error = simState.wrapError("Simulation error");
				break;
			}
			var state = simState.value();
			var lcaResults = calculateSystems(state, progress);
			if (lcaResults.isError()) {
				error = lcaResults.wrapError(
					"Calculation failed in iteration: " + state.iteration());
				break;
			}
			result.appendDispose(state, lcaResults.value());
			progress.tick();
		}
	}

	private Res<List<LcaResult>> calculateSystems(
		SimulationState state, Progress progress
	) {
		var results = new ArrayList<LcaResult>();
		for (var sysLink : model.lca().systemBindings()) {
			if (progress.isCanceled())
				break;
			var res = calculateSystem(state, sysLink);
			if (res.isError()) {
				return res.wrapError("Calculation of system failed: " + sysLink);
			}
			results.add(res.value());
		}
		return Res.ok(results);
	}

	private Res<LcaResult> calculateSystem(
		SimulationState state, SystemBinding sysLink
	) {
		var params = new ArrayList<ParameterRedef>();
		for (var varLink : sysLink.varBindings()) {
			var param = resolver.paramOf(sysLink, varLink);
			if (param == null) {
				return Res.error("Failed to resolve variable binding: " + varLink);
			}
			var amount = amountOf(varLink.varId(), state);
			if (amount.isError()) {
				return amount.wrapError(
					"Failed to resolve variable binding in: " + sysLink);
			}
			param.value = amount.value();
			params.add(param);
		}

		var setup = setupOf(state, sysLink, params);
		if (setup.isError()) {
			return setup.wrapError(
				"Failed to create calculation setup for: " + sysLink);
		}
		try {
			var result = calculator.calculate(setup.value());
			return Res.ok(result);
		} catch (Exception e) {
			return Res.error("Calculation failed: " + sysLink, e);
		}
	}

	private Res<CalculationSetup> setupOf(
		SimulationState state, SystemBinding sysLink, List<ParameterRedef> params
	) {
		double amount = sysLink.amount();
		if (sysLink.amountVar() != null) {
			var amountRes = amountOf(sysLink.amountVar(), state);
			if (amountRes.isError()) {
				return amountRes.wrapError(
					"Failed to resolve reference amount of: " + sysLink);
			}
			amount = amountRes.value();
		}

		var system = resolver.systemOf(sysLink);
		if (system == null) {
			return Res.error("Failed to resolve system: " + sysLink);
		}

		var setup = CalculationSetup.of(system)
			.withParameters(params)
			.withAllocation(sysLink.allocation())
			.withAmount(amount)
			.withImpactMethod(resolver.impactMethod());
		return Res.ok(setup);
	}

	private Res<Double> amountOf(Id variable, SimulationState state) {
		var cell = state.valueOf(variable).orElse(null);
		if (cell == null) {
			return Res.error("Variable not found: " + variable);
		}
		return cell instanceof NumCell(double num)
			? Res.ok(num)
			: Res.error("Variable does not evaluate to a number: " + variable);
	}
}
