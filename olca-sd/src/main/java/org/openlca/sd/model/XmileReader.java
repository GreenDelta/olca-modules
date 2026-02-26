package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.openlca.commons.Res;
import org.openlca.sd.model.LookupFunc.Type;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.EqnCell;
import org.openlca.sd.model.cells.LookupCell;
import org.openlca.sd.model.cells.LookupEqnCell;
import org.openlca.sd.model.cells.NonNegativeCell;
import org.openlca.sd.model.cells.TensorCell;
import org.openlca.sd.model.cells.TensorEqnCell;
import org.openlca.sd.xmile.XmiAux;
import org.openlca.sd.xmile.XmiDim;
import org.openlca.sd.xmile.XmiEvaluatable;
import org.openlca.sd.xmile.XmiFlow;
import org.openlca.sd.xmile.XmiGf;
import org.openlca.sd.xmile.XmiStock;
import org.openlca.sd.xmile.Xmile;

class XmileReader {

	private final Xmile xmile;
	private final SdModel model;
	private final HashMap<Id, Dimension> dimensions;

	XmileReader(Xmile xmile) {
		this.xmile = xmile;
		this.model = new SdModel();
		dimensions = new HashMap<>();
		for (var d : xmile.dims()) {
			var dim = dimOf(d);
			dimensions.put(dim.name(), dim);
			model.dimensions().add(dim);
		}
	}

	private Dimension dimOf(XmiDim d) {
		var id = Id.of(d.name());
		int n = d.elems().size();
		var elements = new Id[n];
		for (int i = 0; i < d.elems().size(); i++) {
			var elem = d.elems().get(i);
			elements[i] = Id.of(elem.name());
		}
		return new Dimension(id, elements);
	}

	Res<SdModel> read() {
		var xmiModel = xmile.model();
		if (xmiModel == null)
			return Res.error("no model found");

		var header = xmile.header();
		if (header != null) {
			model.setId(header.uuid());
			model.setName(header.name());
		}

		var time = simSpecsOf(xmile);
		if (time.isError())
			return time.castError();
		model.setTime(time.value());

		for (var v : xmiModel.variables()) {
			if (!(v instanceof XmiEvaluatable eva))
				continue;
			var cell = cellOf(eva);
			if (cell.isError())
				return cell.wrapError("failed to create cell for: " + eva.name());

			switch (eva) {
				case XmiAux ignored -> {
					var aux = new Auxil(Id.of(eva.name()), cell.value(), eva.units());
					model.vars().add(aux);
				}
				case XmiFlow ignored -> {
					var flow = new Rate(Id.of(eva.name()), cell.value(), eva.units());
					model.vars().add(flow);
				}
				case XmiStock s -> {
					var stock = new Stock(
						Id.of(eva.name()),
						cell.value(),
						eva.units(),
						Id.allOf(s.inflows()),
						Id.allOf(s.outflows()));
					model.vars().add(stock);
				}
			}
		}
		return Res.ok(model);
	}

	private Res<SimSpecs> simSpecsOf(Xmile xmile) {
		if (xmile == null || xmile.simSpecs() == null)
			return Res.error("no sim-specs provided");
		var specs = xmile.simSpecs();
		if (specs.start() == null)
			return Res.error("no start time provided");
		if (specs.stop() == null)
			return Res.error("no end time provided");

		var unit = specs.timeUnits();
		if (specs.dt() == null || specs.dt().value() == null)
			return Res.ok(new SimSpecs(specs.start(), specs.stop(), unit));

		double dt = specs.dt().value();
		var seq = specs.dt().isReciprocal()
				? new SimSpecs(specs.start(), specs.stop(), 1 / dt, unit)
				: new SimSpecs(specs.start(), specs.stop(), dt, unit);
		return Res.ok(seq);
	}

	private Res<Cell> cellOf(XmiEvaluatable v) {
		if (v == null)
			return Res.error("variable is null");

		var eqnRes = cellOf(v.gf(), v.eqn());
		if (eqnRes.isError()) {
			return eqnRes.wrapError("Failed to create cell for variable: "
				+ v.name());
		}

		var tensRes = tensorOf(v);
		if (tensRes.isError()) {
			return tensRes.wrapError("Failed to create tensor for variable: "
				+ v.name());
		}

		var eqn = eqnRes.value().orElse(null);
		var tensor = tensRes.value().orElse(null);

		if (eqn == null && tensor == null) {
			return Res.error("No equation or tensor defined for variable: "
				+ v.name());
		}

		Cell cell;
		if (eqn != null && tensor != null) {
			cell = new TensorEqnCell(eqn, tensor);
		} else if (tensor != null) {
			cell = new TensorCell(tensor);
		} else {
			cell = eqn;
		}

		return v.isNonNegative()
			? Res.ok(new NonNegativeCell(cell))
			: Res.ok(cell);
	}

	private Res<Optional<Cell>> cellOf(XmiGf xmiGf, String eqn) {
		if (xmiGf != null) {
			var gf = funcOf(xmiGf);
			if (gf.isError())
				return gf.castError();
			var cell = Id.isNil(eqn)
				? new LookupCell(gf.value())
				: new LookupEqnCell(eqn, gf.value());
			return Res.ok(Optional.of(cell));
		}
		return Id.isNil(eqn)
			? Res.ok(Optional.empty())
			: Res.ok(Optional.of(new EqnCell(eqn)));
	}

	private Res<Optional<Tensor>> tensorOf(XmiEvaluatable x) {
		if (x.dimensions().isEmpty())
			return Res.ok(Optional.empty());
		var dims = dimsOf(x);
		if (dims.isError())
			return dims.castError();

		var tensor = Tensor.of(dims.value());
		if (x.elements().isEmpty())
			return Res.ok(Optional.of(tensor));

		for (var elem : x.elements()) {
			var subs = Subscript.parseAllFrom(elem.subscript());
			if (subs.isEmpty())
				return Res.error(
					"array elements defined without subscripts in: " + x.name());
			var cellRes = cellOf(elem.gf(), elem.eqn());
			if (cellRes.isError())
				return Res.error("Failed to parse element '"
					+ elem.subscript() + "' in var " + x.name());

			var elemCell = cellRes.value().orElse(null);
			if (elemCell == null)
				continue;
			if (elem.isNonNegative()) {
				elemCell = new NonNegativeCell(elemCell);
			}
			tensor.set(subs, elemCell);
		}
		return Res.ok(Optional.of(tensor));
	}

	private Res<List<Dimension>> dimsOf(XmiEvaluatable v) {
		var dims = new ArrayList<Dimension>();
		for (var d : v.dimensions()) {
			var dim = dimensions.get(Id.of(d.name()));
			if (dim == null)
				return Res.error("unknown dimension: " + d.name());
			dims.add(dim);
		}
		return Res.ok(dims);
	}

	private Res<LookupFunc> funcOf(XmiGf gf) {
		if (gf == null)
			return Res.error("no lookup function defined");

		var type = switch (gf.type()) {
			case CONTINUOUS -> Type.CONTINUOUS;
			case DISCRETE -> Type.DISCRETE;
			case EXTRAPOLATE -> Type.EXTRAPOLATE;
			case null -> Type.CONTINUOUS;
		};

		if (gf.ypts() == null)
			return Res.error("y-values not provided in lookup function");
		var ys = gf.ypts().parse();

		if (gf.xpts() != null)
			return Res.ok(new LookupFunc(type, gf.xpts().parse(), ys));

		if (gf.xscale() != null) {
			var min = gf.xscale().min();
			var max = gf.xscale().max();
			return Res.ok(new LookupFunc(type, min, max, ys));
		}

		return Res.error("no x-values defined in lookup function");
	}
}
