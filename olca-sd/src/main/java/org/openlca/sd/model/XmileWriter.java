package org.openlca.sd.model;

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
import org.openlca.sd.xmile.XmiAux;
import org.openlca.sd.xmile.XmiDim;
import org.openlca.sd.xmile.XmiElement;
import org.openlca.sd.xmile.XmiEvaluatable;
import org.openlca.sd.xmile.XmiFlow;
import org.openlca.sd.xmile.XmiGf;
import org.openlca.sd.xmile.XmiGfType;
import org.openlca.sd.xmile.XmiHeader;
import org.openlca.sd.xmile.XmiMinMax;
import org.openlca.sd.xmile.XmiModel;
import org.openlca.sd.xmile.XmiPoints;
import org.openlca.sd.xmile.XmiProduct;
import org.openlca.sd.xmile.XmiSimSpecs;
import org.openlca.sd.xmile.XmiSmile;
import org.openlca.sd.xmile.XmiStock;
import org.openlca.sd.xmile.XmiVariable;
import org.openlca.sd.xmile.Xmile;
import org.openlca.sd.xmile.lca.XmiEntityRef;
import org.openlca.sd.xmile.lca.XmiLca;
import org.openlca.sd.xmile.lca.XmiSystemBinding;
import org.openlca.sd.xmile.lca.XmiVarBinding;
import org.openlca.sd.xmile.view.XmiAuxView;
import org.openlca.sd.xmile.view.XmiFlowView;
import org.openlca.sd.xmile.view.XmiStockView;
import org.openlca.sd.xmile.view.XmiView;

class XmileWriter {

	private final SdModel model;

	XmileWriter(SdModel model) {
		this.model = model;
	}

	Xmile write() {
		var xmile = new Xmile();
		if (model == null) return xmile;
		putHeader(xmile);
		putSimSpecs(xmile);
		putDims(xmile);

		var xmiModel = new XmiModel();
		xmile.setModel(xmiModel);
		putVariables(xmiModel);
		putExtensions(xmile);
		putViews(xmiModel);
		return xmile;
	}

	private void putHeader(Xmile xmile) {
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
		xmile.setHeader(header);
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

	private void putSimSpecs(Xmile xmile) {
		var specs = model.simSpecs();
		if (specs == null) return;
		var x = new XmiSimSpecs();
		x.setStart(specs.start());
		x.setStop(specs.end());
		x.setTimeUnits(specs.unit());
		var dt = new XmiSimSpecs.DeltaT();
		dt.setValue(specs.dt());
		x.setDt(dt);
		xmile.setSimSpecs(x);
	}

	private void putDims(Xmile xmile) {
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

	private void putVariables(XmiModel xmiModel) {
		for (var v : model.vars()) {
			XmiVariable xmiVar = switch (v) {
				case Auxil a -> xmiAuxOf(a);
				case Rate r -> xmiFlowOf(r);
				case Stock s -> xmiStockOf(s);
			};
			xmiModel.variables().add(xmiVar);
		}
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
		s.inFlows().stream().map(Id::value).forEach(x.inflows()::add);
		s.outFlows().stream().map(Id::value).forEach(x.outflows()::add);
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
		for (var d : t.dimensions()) {
			var xmiDim = new XmiEvaluatable.Dim();
			xmiDim.setName(d.name().value());
			x.dimensions().add(xmiDim);
		}

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
			x.elements().add(elem);
		}
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

	private void putExtensions(Xmile xmile) {
		var lca = model.lca();
		if (lca.impactMethod() == null && lca.systemBindings().isEmpty())
			return;

		var ex = new XmiLca();
		if (lca.impactMethod() != null) {
			ex.setImpactMethod(xmiRefOf(lca.impactMethod()));
		}

		for (var b : lca.systemBindings()) {
			var xsb = new XmiSystemBinding();
			if (b.system() != null) {
				xsb.setSystem(xmiRefOf(b.system()));
			}
			xsb.setAllocation(b.allocation());
			xsb.setAmount(b.amount());
			if (b.amountVar() != null) {
				xsb.setAmountVar(b.amountVar().label());
			}
			for (var vb : b.varBindings()) {
				var xvb = new XmiVarBinding();
				if (vb.varId() != null) {
					xvb.setVariable(vb.varId().label());
				}
				xvb.setParameter(vb.parameter());
				if (vb.context() != null) {
					xvb.setContext(xmiRefOf(vb.context()));
				}
				xsb.varBindings().add(xvb);
			}
			ex.systemBindings().add(xsb);
		}
		xmile.setLca(ex);
	}

	private XmiEntityRef xmiRefOf(EntityRef ref) {
		var x = new XmiEntityRef();
		x.setName(ref.name());
		x.setId(ref.refId());
		x.setType(ref.type());
		return x;
	}

	private void putViews(XmiModel xmiModel) {
		if (model.positions().isEmpty())	return;

		var view = new XmiView();
		for (var entry : model.positions().entrySet()) {
			var id = entry.getKey();
			var rect = entry.getValue();
			var v = findVar(id);
			if (v == null) continue;

			var vv = switch (v) {
				case Auxil ignored -> {
					var av = new XmiAuxView();
					view.auxiliaries().add(av);
					yield av;
				}
				case Rate ignored -> {
					var fv = new XmiFlowView();
					view.flows().add(fv);
					yield fv;
				}
				case Stock ignored -> {
					var sv = new XmiStockView();
					view.stocks().add(sv);
					yield sv;
				}
			};

			int w = rect.width() > 0 ? rect.width() : 80;
			int h = rect.height() > 0 ? rect.height() : 45;
			double x = rect.x() + w / 2.0;
			double y = rect.y() + h / 2.0;
			vv.setName(id.label());
			vv.setX(x);
			vv.setY(y);
			vv.setWidth((double) w);
			vv.setHeight((double) h);
		}

		xmiModel.viewList().add(view);
	}

	private Var findVar(Id id) {
		for (var v : model.vars()) {
			if (id.equals(v.name()))
				return v;
		}
		return null;
	}

}
