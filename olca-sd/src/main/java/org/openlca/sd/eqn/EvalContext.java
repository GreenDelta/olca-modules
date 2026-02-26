package org.openlca.sd.eqn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.eqn.func.Abs;
import org.openlca.sd.eqn.func.ArcCos;
import org.openlca.sd.eqn.func.ArcSin;
import org.openlca.sd.eqn.func.ArcTan;
import org.openlca.sd.eqn.func.Cos;
import org.openlca.sd.eqn.func.Exp;
import org.openlca.sd.eqn.func.ExpRnd;
import org.openlca.sd.eqn.func.Func;
import org.openlca.sd.eqn.func.Int;
import org.openlca.sd.eqn.func.Ln;
import org.openlca.sd.eqn.func.Log10;
import org.openlca.sd.eqn.func.LogNormal;
import org.openlca.sd.eqn.func.Max;
import org.openlca.sd.eqn.func.Min;
import org.openlca.sd.eqn.func.Mod;
import org.openlca.sd.eqn.func.Normal;
import org.openlca.sd.eqn.func.Poisson;
import org.openlca.sd.eqn.func.Random;
import org.openlca.sd.eqn.func.Sin;
import org.openlca.sd.eqn.func.Sqrt;
import org.openlca.sd.eqn.func.Sum;
import org.openlca.sd.eqn.func.Tan;
import org.openlca.sd.model.Auxil;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Var;

public class EvalContext {

	private final Map<Id, Var> vars = new HashMap<>();
	private final Map<Id, Func> funcs = new HashMap<>();

	public EvalContext() {

		// bind default functions
		bind(new Abs());
		bind(new Sum());
		bind(new Max());
		bind(new Min());
		bind(new Sqrt());
		bind(new Sin());
		bind(new Cos());
		bind(new Tan());
		bind(new Exp());
		bind(new Int());
		bind(new Ln());
		bind(new Log10());
		bind(new Poisson());
		bind(new Normal());
		bind(new ExpRnd());
		bind(new LogNormal());
		bind(new Random());

		// we do not bind standard math functions for now
		// bind(new Add());
		// bind(new Sub());
		// bind(new Neg());
		// bind(new Mul());
		// bind(new Div());
		bind(new Mod());

		// ARC functions with their default names and aliases
		var arcCos = new ArcCos();
		bind(arcCos);
		funcs.put(Id.of("ACOS"), arcCos);

		var arcSin = new ArcSin();
		bind(arcSin);
		funcs.put(Id.of("ASIN"), arcSin);

		var arcTan = new ArcTan();
		bind(arcTan);
		funcs.put(Id.of("ATAN"), arcTan);
	}

	public EvalContext bind(Var var) {
		if (var != null) {
			vars.put(var.name(), var);
		}
		return this;
	}

	public EvalContext bind(String name, double value) {
		var v = new Auxil(Id.of(name), Cell.of(value), "-");
		return bind(v);
	}

	public Optional<Var> getVar(Id name) {
		return name != null
			? Optional.ofNullable(vars.get(name))
			: Optional.empty();
	}

	public EvalContext bind(Func func) {
		if (func != null) {
			funcs.put(func.name(), func);
		}
		return this;
	}

	public Optional<Func> getFunc(Id name) {
		return name != null
			? Optional.ofNullable(funcs.get(name))
			: Optional.empty();
	}

}
