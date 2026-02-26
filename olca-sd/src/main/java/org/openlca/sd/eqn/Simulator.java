package org.openlca.sd.eqn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.func.Add;
import org.openlca.sd.eqn.func.Mul;
import org.openlca.sd.eqn.func.NonNeg;
import org.openlca.sd.eqn.func.Sub;
import org.openlca.sd.eqn.func.Sum;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.SimSpecs;
import org.openlca.sd.model.Stock;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.Var;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NonNegativeCell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;
import org.openlca.sd.xmile.Xmile;

public class Simulator implements Iterable<Res<SimulationState>> {

	private final SdModel model;

	private Simulator(SdModel model) {
		this.model = model;
	}

	public static Res<Simulator> of(Xmile xmile) {
		var res = SdModel.readFrom(xmile);
		if (res.isError()) {
			return res.wrapError("failed to parse model from xmile");
		}
		var model = res.value();
		var order = EvaluationOrder.of(model.vars());
		if (order.isError()) {
			return order.wrapError("failed to determine evaluation order");
		}
		model.vars().clear();
		model.vars().addAll(order.value());
		return Res.ok(new Simulator(model));
	}

	@Override
	public Simulation iterator() {
		var freshVars = new ArrayList<Var>(model.vars().size());
		for (var v : model.vars()) {
			freshVars.add(v.freshCopy());
		}
		return new Simulation(model.time(), freshVars);
	}

	public static class Simulation implements Iterator<Res<SimulationState>> {

		private final double start;
		private final double end;
		private final double dt;

		private final List<Var> vars;
		private final Map<Id, Var> state;

		private final EvalContext ctx;
		private final Auxil timeVar;
		private final Cell dtCell;
		private final Interpreter interpreter;

		private boolean isInitialized = false;
		private int iteration;
		private final List<Stock> stocks = new ArrayList<>();
		private final List<Var> evalVars = new ArrayList<>();

		private Simulation(SimSpecs time, List<Var> vars) {
			this.start = time.start();
			this.end = time.end();
			this.dt = time.dt();

			this.vars = vars;
			this.state = new HashMap<>();
			for (var v : vars) {
				state.put(v.name(), v);
			}

			ctx = new EvalContext();
			ctx.bind("INF", Double.POSITIVE_INFINITY);
			ctx.bind("PI", Math.PI);
			ctx.bind("DT", dt);
			ctx.bind("STARTTIME", start);
			ctx.bind("STOPTIME", end);
			// TODO: bind top-level lookup functions

			timeVar = new Auxil(Id.of("TIME"), Cell.of(start), time.unit());
			dtCell = Cell.of(dt);
			ctx.bind(timeVar);
			for (var v : vars) {
				ctx.bind(v);
			}

			interpreter = Interpreter.of(ctx);
		}

		public Interpreter interpreter() {
			return interpreter;
		}

		@Override
		public boolean hasNext() {
			return !isInitialized || timeAt(iteration) < end;
		}

		@Override
		public Res<SimulationState> next() {
			if (!hasNext()) {
				throw new IllegalStateException("simulation finished");
			}
			return !isInitialized
				? initialize()
				: nextState();
		}

		private double timeAt(int iteration) {
			return start + iteration * dt;
		}

		private Res<SimulationState> initialize() {
			isInitialized = true;
			timeVar.pushValue(Cell.of(start));

			for (var v : vars) {
				var res = v.def().eval(interpreter);
				if (res.isError()) {
					return res.wrapError("Initialization of variable '"
						+ v.name().label() + "' failed");
				}
				v.pushValue(res.value());
				if (v instanceof Stock stock) {
					stocks.add(stock);
				} else {
					evalVars.add(v);
				}
			}
			return Res.ok(new SimulationState(0, start, state));
		}

		private Res<SimulationState> nextState() {

			// update the stocks
			for (var stock : stocks) {
				var stockVal = stock.value();

				// adding the in-flows
				for (var flowId : stock.inFlows()) {
					var flowDelta = flowDelta(stockVal, flowId);
					if (flowDelta.isError())
						return Res.error("Failed to evaluate input flow: " + flowId);

					var nextVal = Add.apply(stockVal, flowDelta.value());
					if (nextVal.isError()) {
						return nextVal.wrapError("Failed to add flow " + flowId
							+ " to stock " + stock.name());
					}
					stockVal = nextVal.value();
				}

				// subtracting the out-flows
				for (var flowId : stock.outFlows()) {
					var flowDelta = flowDelta(stockVal, flowId);
					if (flowDelta.isError())
						return Res.error("Failed to evaluate output flow: " + flowId);

					var nextVal = Sub.apply(stockVal, flowDelta.value());
					if (nextVal.isError()) {
						return nextVal.wrapError("Failed to subtract out-flow " + flowId
							+ " from stock " + stock.name());
					}
					stockVal = nextVal.value();
				}

				if (stock.def() instanceof NonNegativeCell) {
					var nonNeg = new NonNeg().apply(List.of(stockVal));
					if (nonNeg.isError()) {
						return nonNeg.wrapError(
							"Failed to apply NonNeg on stock " + stock.name());
					}
					stockVal = nonNeg.value();
				}
				stock.pushValue(stockVal);
			}

			// evaluate the variables
			for (var v : evalVars) {
				var res = v.def().eval(interpreter);
				if (res.isError())
					return res.wrapError("Evaluation error for variable: " + v.name());
				v.pushValue(res.value());
			}

			iteration++;
			double t = timeAt(iteration);
			timeVar.pushValue(Cell.of(t));
			return Res.ok(new SimulationState(iteration, t, state));
		}

		private Res<Cell> flowDelta(Cell stock, Id flowId) {
			var flow = ctx.getVar(flowId).orElse(null);
			if (flow == null)
				return Res.error("Flow is not defined: " + flowId);
			var res = Mul.apply(flow.value(), dtCell);
			if (res.isError())
				return res.wrapError("Failed to calculate: " + flowId + " * dt");
			var cell = res.value();
			if (cell instanceof TensorCell(Tensor flowTensor)) {
				return switch (stock) {
					case TensorCell(Tensor stockTensor) -> {
						var t = TensorProjection.of(flowTensor, stockTensor.dimensions());
						yield t.isError()
							? t.wrapError("Failed to project flow tensor to stock dimension")
							: Res.ok(Cell.of(t.value()));
					}
					case NumCell ignore -> Sum.of(flowTensor);
					case null, default -> Res.ok(cell);
				};
			}
			return Res.ok(cell);
		}
	}
}
