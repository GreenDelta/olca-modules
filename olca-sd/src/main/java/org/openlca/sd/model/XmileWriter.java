package org.openlca.sd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.commons.Strings;
import org.openlca.sd.model.cells.BoolCell;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.EmptyCell;
import org.openlca.sd.model.cells.EqnCell;
import org.openlca.sd.model.cells.LookupCell;
import org.openlca.sd.model.cells.LookupEqnCell;
import org.openlca.sd.model.cells.NonNegativeCell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;
import org.openlca.sd.model.cells.TensorEqnCell;
import org.openlca.sd.util.Tensors;
import org.openlca.sd.xmile.*;
import org.openlca.sd.xmile.view.*;
import org.openlca.sd.xmile.lca.*;

class XmileWriter {

	private final SdModel model;

	XmileWriter(SdModel model) {
		this.model = model;
	}

	Xmile write() {
		var xmile = new Xmile();
		if (model == null) return xmile;
		xmile.setHeader(writeHeader());
		xmile.setSimSpecs(writeSimSpecs());
		writeDimsTo(xmile);
		var xmiModel = new XmiModel();
		xmiModel.setVariables(writeVariables());
		xmiModel.setViews(writeViews());
		xmile.setModel(xmiModel);
		xmile.setLca(writeExtensions());
		return xmile;
	}

	private List<Object> writeViews() {
		if (model.positions().isEmpty())
			return Collections.emptyList();

		var view = new XmiView();
		for (var entry : model.positions().entrySet()) {
			var id = entry.getKey();
			var rect = entry.getValue();
			var v = findVar(id);
			if (v == null)
				continue;

			int w = rect.width() > 0 ? rect.width() : 80;
			int h = rect.height() > 0 ? rect.height() : 45;
			double x = rect.x() + w / 2.0;
			double y = rect.y() + h / 2.0;

			switch (v) {
				case Auxil ignored -> {
					var av = new XmiAuxView();
					writePosition(av, id, x, y, w, h);
					view.auxiliaries().add(av);
				}
				case Rate ignored -> {
					var fv = new XmiFlowView();
					writePosition(fv, id, x, y, w, h);
					view.flows().add(fv);
				}
				case Stock ignored -> {
					var sv = new XmiStockView();
					writePosition(sv, id, x, y, w, h);
					view.stocks().add(sv);
				}
			}
		}

		return List.of(view);
	}

	private Var findVar(Id id) {
		for (var v : model.vars()) {
			if (id.equals(v.name()))
				return v;
		}
		return null;
	}

	private void writePosition(
		XmiVariableView v, Id id,
		double x, double y, int w, int h) {
		v.setName(id.label());
		v.setX(x);
		v.setY(y);
		v.setWidth((double) w);
		v.setHeight((double) h);
	}

	private XmiLca writeExtensions() {
		if (model.method() == null && model.systemBindings().isEmpty())
			return null;

		var ex = new XmiLca();
		if (model.method() != null) {
			ex.impactMethod = model.method().refId;
		}

		for (var b : model.systemBindings()) {
			var xsb = new XmiSystemBinding();
			if (b.system() != null) {
				xsb.system = b.system().refId;
			}
			xsb.allocation = b.allocation();
			xsb.amount = b.amount();
			for (var vb : b.varBindings()) {
				var xvb = new XmiVarBinding();
				xvb.var = vb.varId() != null ? vb.varId().label() : null;
				if (vb.parameter() != null) {
					var xp = new XmiParameter();
					var p = vb.parameter();
					xp.name = p.name;
					xp.value = p.value;
					xp.description = p.description;
					xp.contextId = p.contextId != null ? p.contextId.toString() : null;
					xp.contextType = p.contextType;
					xvb.parameter = xp;
				}
				xsb.varBindings.add(xvb);
			}
			ex.systemBindings.add(xsb);
		}
		return ex;
	}

	private XmiHeader writeHeader() {
		var header = new XmiHeader();
		header.setVendor("openLCA.org");
		header.setUuid(model.id());
		header.setName(model.name());
		var product = new XmiProduct();
		product.setLang("en");
		product.setVersion("2.7");
		product.setValue("openLCA");
		header.setProduct(product);

		var smile = new XmiSmile();
		smile.setVersion("1.0");
		smile.setUsesArrays(Integer.toString(getMaxArrayDimension()));
		smile.setNamespace("std, olca");
		header.setSmile(smile);
		return header;
	}

	private int getMaxArrayDimension() {
		if (model == null) return 0;
		int maxDim = 0;
		for (var v : model.vars()) {
			var tensor = tensorOf(v.def());
			if (tensor != null) {
				maxDim = Math.max(maxDim, tensor.dimensions().size());
			}
		}
		return maxDim;
	}

	private Tensor tensorOf(Cell cell) {
		return switch (cell) {
			case TensorCell(Tensor t) -> t;
			case TensorEqnCell(Cell ignore, Tensor t) -> t;
			case NonNegativeCell(Cell c) -> tensorOf(c);
			case null, default -> null;
		};
	}

	private XmiSimSpecs writeSimSpecs() {
		var specs = model.time();
		if (specs == null) return null;
		var x = new XmiSimSpecs();
		x.setStart(specs.start());
		x.setStop(specs.end());
		x.setTimeUnits(specs.unit());
		var dt = new XmiSimSpecs.DeltaT();
		dt.setValue(specs.dt());
		x.setDt(dt);
		return x;
	}

	private void writeDimsTo(Xmile xmile) {
		for (var d : model.dimensions()) {
			var xd = new XmiDim();
			xd.setName(d.name().value());
			for (var e : d.elements()) {
				var xe = new XmiDim.Elem();
				xe.setName(e.value());
				xd.elems().add(xe);
			}
			xmile.dims().add(xd);
		}
	}

	private List<XmiVariable> writeVariables() {
		var vars = new ArrayList<XmiVariable>();
		for (var v : model.vars()) {
			XmiVariable xmiVar = switch (v) {
				case Auxil a -> xmiAuxOf(a);
				case Rate r -> xmiFlowOf(r);
				case Stock s -> xmiStockOf(s);
			};
			vars.add(xmiVar);
		}
		return vars;
	}

	private XmiAux xmiAuxOf(Auxil a) {
		var x = new XmiAux();
		x.setName(a.name().label());
		x.setUnits(a.unit());
		fillVariable(x, a.def());
		return x;
	}

	private XmiFlow xmiFlowOf(Rate r) {
		var x = new XmiFlow();
		x.setName(r.name().label());
		x.setUnits(r.unit());
		fillVariable(x, r.def());
		return x;
	}

	private XmiStock xmiStockOf(Stock s) {
		var x = new XmiStock();
		x.setName(s.name().label());
		x.setUnits(s.unit());
		fillVariable(x, s.def());
		x.setInflows(s.inFlows().stream().map(Id::value).toList());
		x.setOutflows(s.outFlows().stream().map(Id::value).toList());
		return x;
	}

	private void fillVariable(XmiEvaluatable x, Cell cell) {
		switch (cell) {
			case BoolCell(boolean b) -> x.setEqn(Boolean.toString(b));
			case EmptyCell ignore -> {
			}
			case EqnCell(String eqn) -> x.setEqn(eqn);
			case LookupCell(LookupFunc func) -> x.setGf(xmiLookupOf(func));
			case NumCell(double num) -> x.setEqn(Double.toString(num));
			case TensorCell(Tensor tensor) -> fillTensor(x, tensor);

			case LookupEqnCell(String eqn, LookupFunc func) -> {
				x.setEqn(eqn);
				x.setGf(xmiLookupOf(func));
			}
			case NonNegativeCell(Cell value) -> {
				x.setNonNegative();
				fillVariable(x, value);
			}
			case TensorEqnCell(Cell eqn, Tensor tensor) -> {
				fillVariable(x, eqn);
				fillTensor(x, tensor);
			}
		}
	}

	private void fillTensor(XmiEvaluatable x, Tensor t) {
		var dims = new ArrayList<XmiEvaluatable.Dim>();
		for (var d : t.dimensions()) {
			var xmiDim = new XmiEvaluatable.Dim();
			xmiDim.setName(d.name().value());
			dims.add(xmiDim);
		}
		x.setDimensions(dims);

		var elements = new ArrayList<XmiElement>();
		for (var a : Tensors.addressesOf(t)) {
			var cell = t.get(a);
			if (cell.isEmpty()) continue;
			var elem = new XmiElement();
			fillElement(elem, cell);
			if (Strings.isBlank(elem.eqn()) && elem.gf() == null) {
				continue;
			}
			var subscript = a.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", "));
			elem.setSubscript(subscript);
			elements.add(elem);
		}
		x.setElements(elements);
	}

	private void fillElement(XmiElement x, Cell cell) {
		switch (cell) {
			case BoolCell(boolean b) -> x.setEqn(Boolean.toString(b));
			case EmptyCell ignore -> {
			}
			case EqnCell(String eqn) -> x.setEqn(eqn);
			case LookupCell(LookupFunc func) -> x.setGf(xmiLookupOf(func));
			case NumCell(double num) -> x.setEqn(Double.toString(num));
			case LookupEqnCell(String eqn, LookupFunc func) -> {
				x.setEqn(eqn);
				x.setGf(xmiLookupOf(func));
			}
			case NonNegativeCell(Cell value) -> {
				x.setNonNegative();
				fillElement(x, value);
			}
			case TensorCell ignore -> {
			}
			case TensorEqnCell ignore -> {
			}
		}
	}

	private XmiGf xmiLookupOf(LookupFunc func) {
		if (func == null)
			return null;

		var xmiGf = new XmiGf();
		xmiGf.setType(switch (func.type()) {
			case DISCRETE -> XmiGfType.DISCRETE;
			case EXTRAPOLATE -> XmiGfType.EXTRAPOLATE;
			case null, default -> XmiGfType.CONTINUOUS;
		});

		// in our runtime-model, we always translate range defined lookup functions
		// into their xy-pairs. Thus, we write this back to XMILE and these values
		// should be read first in an import. We additionally provide the x-range
		// for information and debugging.
		xmiGf.setYpts(xmiPointsOf(func.ys()));
		var xs = func.xs();
		xmiGf.setXpts(xmiPointsOf(xs));
		if (xs != null && xs.length > 0) {
			var scale = new XmiMinMax();
			scale.setMin(xs[0]);
			scale.setMax(xs[xs.length - 1]);
			xmiGf.setXscale(scale);
		}
		return xmiGf;
	}

	private XmiPoints xmiPointsOf(double[] values) {
		if (values == null)
			return null;
		var sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(values[i]);
		}
		var points = new XmiPoints();
		points.setValues(sb.toString());
		return points;
	}
}
