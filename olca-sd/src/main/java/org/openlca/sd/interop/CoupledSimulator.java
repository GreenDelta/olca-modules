package org.openlca.sd.interop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.sd.eqn.SimulationState;
import org.openlca.sd.eqn.Simulator;
import org.openlca.sd.model.EntityRef;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.SystemBinding;
import org.openlca.sd.model.VarBinding;
import org.openlca.sd.model.cells.NumCell;

public class CoupledSimulator {

	private final Simulator simulator;
	private final SystemCalculator calculator;
	private final ImpactMethod impactMethod;
	private final List<ResolvedBinding> bindings;
	private final CoupledResult result;
	private Res<?> error;

	private record ResolvedBinding(
			SystemBinding binding,
			ProductSystem system,
			List<ResolvedVarBinding> varBindings) {
	}

	private record ResolvedVarBinding(
			VarBinding binding,
			ParameterRedef template) {
	}

	private CoupledSimulator(
			Simulator simulator,
			SystemCalculator calculator,
			ImpactMethod impactMethod,
			List<ResolvedBinding> bindings) {
		this.simulator = simulator;
		this.calculator = calculator;
		this.impactMethod = impactMethod;
		this.bindings = bindings;
		this.result = new CoupledResult();
	}

	public static Res<CoupledSimulator> of(
			SdModel model, IDatabase db, SystemCalculator calculator) {

		// create the simulator
		var sim = Simulator.of(model);
		if (sim.isError())
			return sim.wrapError("failed to create simulator");

		// resolve the impact method
		var lca = model.lca();
		ImpactMethod method = null;
		if (lca.impactMethod() != null) {
			var ref = lca.impactMethod();
			method = db.get(ImpactMethod.class, ref.refId());
			if (method == null)
				return Res.error("impact method not found: " + ref.name());
		}

		// resolve system bindings
		var resolved = new ArrayList<ResolvedBinding>();
		for (var b : lca.systemBindings()) {
			var sysRef = b.system();
			if (sysRef == null)
				return Res.error("system binding without system reference");
			var system = db.get(ProductSystem.class, sysRef.refId());
			if (system == null)
				return Res.error("product system not found: " + sysRef.name());

			// resolve variable bindings
			var varBindings = new ArrayList<ResolvedVarBinding>();
			for (var vb : b.varBindings()) {
				if (vb.varId() == null || vb.parameter() == null)
					continue;
				var template = new ParameterRedef();
				template.name = vb.parameter();
				if (vb.context() != null) {
					var ctx = vb.context();
					template.contextType = ctx.type();
					var ctxEntity = db.get(
							ctx.type().getModelClass(), ctx.refId());
					if (ctxEntity == null)
						return Res.error(
								"parameter context not found: " + ctx.name());
					template.contextId = ctxEntity.id;
				}
				varBindings.add(new ResolvedVarBinding(vb, template));
			}
			resolved.add(new ResolvedBinding(b, system, varBindings));
		}

		return Res.ok(new CoupledSimulator(
				sim.value(), calculator, method, resolved));
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
			for (var rb : bindings) {
				if (progress.isCanceled())
					break;

				var params = paramsOf(simState, rb);
				if (params.isError()) {
					error = params.wrapError("Variable binding error");
					break;
				}

				var calcSetup = CalculationSetup.of(rb.system())
						.withParameters(params.value())
						.withAllocation(rb.binding().allocation());
				if (impactMethod != null) {
					calcSetup = calcSetup.withImpactMethod(impactMethod);
				}
				calcSetup = calcSetup.withAmount(rb.binding().amount());

				try {
					var lcaResult = calculator.calculate(calcSetup);
					rs.add(lcaResult);
				} catch (Exception e) {
					error = Res.error(
							"Calculation of system failed: "
									+ rb.binding().system().name(), e);
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
			SimulationState simState, ResolvedBinding rb) {

		var params = new ArrayList<ParameterRedef>();
		for (var rvb : rb.varBindings()) {
			var vb = rvb.binding();
			var cell = simState.valueOf(vb.varId()).orElse(null);
			if (cell == null)
				return Res.error("Variable not found: " + vb.varId());
			if (!(cell instanceof NumCell(double num))) {
				return Res.error(
						"Variable does not evaluate to a number: " + vb.varId());
			}

			var param = rvb.template().copy();
			param.value = num;
			params.add(param);
		}
		return Res.ok(params);
	}
}
