package org.openlca.sd.eqn;

import java.util.ArrayList;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.eqn.func.Add;
import org.openlca.sd.eqn.func.Div;
import org.openlca.sd.eqn.func.Eq;
import org.openlca.sd.eqn.func.Mod;
import org.openlca.sd.eqn.func.Mul;
import org.openlca.sd.eqn.func.Neg;
import org.openlca.sd.eqn.func.Pow;
import org.openlca.sd.eqn.func.Sub;
import org.openlca.sd.eqn.generated.EqnBaseVisitor;
import org.openlca.sd.eqn.generated.EqnParser;
import org.openlca.sd.eqn.generated.EqnParser.AddSubContext;
import org.openlca.sd.eqn.generated.EqnParser.ArrayAccessContext;
import org.openlca.sd.eqn.generated.EqnParser.CompContext;
import org.openlca.sd.eqn.generated.EqnParser.FunCallContext;
import org.openlca.sd.eqn.generated.EqnParser.IdSubscriptContext;
import org.openlca.sd.eqn.generated.EqnParser.IfThenElseContext;
import org.openlca.sd.eqn.generated.EqnParser.IntSubscriptContext;
import org.openlca.sd.eqn.generated.EqnParser.LogicContext;
import org.openlca.sd.eqn.generated.EqnParser.MulDivContext;
import org.openlca.sd.eqn.generated.EqnParser.NotContext;
import org.openlca.sd.eqn.generated.EqnParser.NumberContext;
import org.openlca.sd.eqn.generated.EqnParser.ParensContext;
import org.openlca.sd.eqn.generated.EqnParser.PowerContext;
import org.openlca.sd.eqn.generated.EqnParser.UnarySignContext;
import org.openlca.sd.eqn.generated.EqnParser.VarContext;
import org.openlca.sd.model.Id;

class EvalVisitor extends EqnBaseVisitor<Res<Cell>> {

	private final EvalContext evalCtx;

	EvalVisitor(EvalContext evalCtx) {
		this.evalCtx = Objects.requireNonNull(evalCtx);
	}

	@Override
	public Res<Cell> visitAddSub(AddSubContext ctx) {
		var a = visit(ctx.eqn(0));
		if (a.isError())
			return a;
		var b = visit(ctx.eqn(1));
		if (b.isError())
			return b;

		return switch (ctx.op.getType()) {
			case EqnParser.ADD -> Add.apply(a.value(), b.value());
			case EqnParser.SUB -> Sub.apply(a.value(), b.value());
			default -> Res.error(
				"operator not supported: " + a.value() + ctx.op.getText() + b.value());
		};
	}

	@Override
	public Res<Cell> visitUnarySign(UnarySignContext ctx) {
		var res = visit(ctx.eqn());
		if (res.isError())
			return res;

		return switch (ctx.op.getType()) {
			case EqnParser.ADD -> res;
			case EqnParser.SUB -> Neg.apply(res.value());
			default -> Res.error(
				"unsupported unary operator " + ctx.op.getText() + res.value());
		};
	}

	@Override
	public Res<Cell> visitMulDiv(MulDivContext ctx) {
		var a = visit(ctx.eqn(0));
		if (a.isError())
			return a;
		var b = visit(ctx.eqn(1));
		if (b.isError())
			return b;

		return switch (ctx.op.getType()) {
			case EqnParser.MUL -> Mul.apply(a.value(), b.value());
			case EqnParser.DIV -> Div.apply(a.value(), b.value());
			case EqnParser.MOD -> Mod.apply(a.value(), b.value());
			default -> Res.error(
				"operator not supported: " + a.value() + ctx.op.getText() + b.value());
		};
	}

	@Override
	public Res<Cell> visitComp(CompContext ctx) {
		var a = visit(ctx.eqn(0));
		if (a.isError())
			return a;
		var b = visit(ctx.eqn(1));
		if (b.isError())
			return b;

		var cellA = a.value();
		var cellB = b.value();

		if (ctx.op.getType() == EqnParser.EQ)
			return Eq.apply(cellA, cellB);
		if (ctx.op.getType() == EqnParser.NEQ) {
			var res = Eq.apply(cellA, cellB);
			if (res.isError())
				return res;
			var resCell = res.value();
			return resCell.isBoolCell()
				? Res.ok(Cell.of(!resCell.asBool()))
				: Res.error("does not evaluate to a boolean");
		}

		if (!cellA.isNumCell() || !cellB.isNumCell())
			return Res.error("operator not supported: "
				+ cellA + ctx.op.getText() + cellB);

		double x = cellA.asNum();
		double y = cellB.asNum();
		return switch (ctx.op.getType()) {
			case EqnParser.GE -> Res.ok(Cell.of(x >= y));
			case EqnParser.GT -> Res.ok(Cell.of(x > y));
			case EqnParser.LE -> Res.ok(Cell.of(x <= y));
			case EqnParser.LT -> Res.ok(Cell.of(x < y));
			default -> Res.error(
				"operator not supported: : " + cellA + ctx.op.getText() + cellB);
		};
	}

	@Override
	public Res<Cell> visitNot(NotContext ctx) {
		var cellRes = visit(ctx.eqn());
		if (cellRes.isError()) return cellRes;

		var cell = cellRes.value();
		if (!cell.isBoolCell())
			return Res.error(
				"NOT operator requires boolean operand, got: " + cell);
		boolean value = cell.asBoolCell().value();
		return Res.ok(Cell.of(!value));
	}

	@Override
	public Res<Cell> visitLogic(LogicContext ctx) {
		var a = visit(ctx.eqn(0));
		if (a.isError())
			return a;
		var b = visit(ctx.eqn(1));
		if (b.isError())
			return b;

		var cellA = a.value();
		var cellB = b.value();
		if (!cellA.isBoolCell() || !cellB.isBoolCell())
			return Res.error(
				"operator not supported: : " + cellA + ctx.op.getText() + cellB);

		boolean x = cellA.asBoolCell().value();
		boolean y = cellB.asBoolCell().value();
		return switch (ctx.op.getType()) {
			case EqnParser.AND -> Res.ok(Cell.of(x && y));
			case EqnParser.OR -> Res.ok(Cell.of(x || y));
			default -> Res.error(
				"operator not supported: : " + cellA + ctx.op.getText() + cellB);
		};
	}

	@Override
	public Res<Cell> visitIfThenElse(IfThenElseContext ctx) {
		var condRes = visit(ctx.eqn(0));
		if (condRes.isError())
			return condRes;

		var cond = condRes.value();
		if (!cond.isBoolCell())
			return Res.error(
				"IF condition must be boolean, got: " + cond);

		return cond.asBoolCell().value()
			? visit(ctx.eqn(1))
			: visit(ctx.eqn(2));
	}

	@Override
	public Res<Cell> visitParens(ParensContext ctx) {
		return visit(ctx.eqn());
	}

	@Override
	public Res<Cell> visitPower(PowerContext ctx) {
		var a = visit(ctx.eqn(0));
		if (a.isError())
			return a;
		var b = visit(ctx.eqn(1));
		if (b.isError())
			return b;
		return Pow.apply(a.value(), b.value());
	}

	@Override
	public Res<Cell> visitNumber(NumberContext ctx) {
		try {
			var num = Double.parseDouble(ctx.getText());
			return Res.ok(Cell.of(num));
		} catch (NumberFormatException e) {
			return Res.error("invalid number format: " + ctx.getText());
		}
	}

	@Override
	public Res<Cell> visitVar(VarContext ctx) {
		var v = evalCtx.getVar(Id.of(ctx.getText())).orElse(null);
		if (v == null)
			return Res.error("unknown variable: " + ctx.getText());
		return Res.ok(v.value());
	}

	@Override
	public Res<Cell> visitArrayAccess(ArrayAccessContext ctx) {
		// Get the variable
		var varName = ctx.ID().getText();
		var v = evalCtx.getVar(Id.of(varName)).orElse(null);
		if (v == null)
			return Res.error("unknown variable: " + varName);

		var cell = v.value();
		if (!cell.isTensorCell())
			return Res.error("variable " + varName + " is not a tensor");

		// Parse subscripts
		var subscripts = new ArrayList<Subscript>();
		for (var subCtx : ctx.subscript()) {
			try {
				if (subCtx instanceof IdSubscriptContext idCtx) {
					var id = Id.of(idCtx.ID().getText());
					subscripts.add(Subscript.of(id));
				} else if (subCtx instanceof IntSubscriptContext intCtx) {
					String numText = intCtx.NUMBER().getText();
					// Check if it's a positive integer (no decimal point, no scientific notation)
					if (numText.contains(".") || numText.toLowerCase().contains("e")) {
						return Res.error("subscript must be a positive integer, got: " + numText);
					}
					int index = Integer.parseInt(numText);
					if (index < 1) {
						return Res.error("subscript must be positive (>= 1), got: " + index);
					}
					// Convert from 1-based to 0-based indexing
					subscripts.add(Subscript.of(index - 1));
				} else {
					return Res.error("unknown subscript type");
				}
			} catch (NumberFormatException e) {
				return Res.error("invalid integer subscript: " + subCtx.getText());
			}
		}

		// Access tensor element
		try {
			var tensor = cell.asTensorCell().value();
			var resultCell = tensor.get(subscripts);
			return Res.ok(resultCell);
		} catch (Exception e) {
			return Res.error("array access failed for " + varName + ": " + e.getMessage());
		}
	}

	@Override
	public Res<Cell> visitFunCall(FunCallContext ctx) {
		var funcId = Id.of(ctx.ID().getText());

		// handle init-function calls
		if ("init".equals(funcId.value())) {
			if (ctx.eqn().size() != 1
				|| !(ctx.eqn().getFirst() instanceof VarContext varCtx)) {
				return Res.error("init-function must have a " +
					"single variable name as argument");
			}
			var varId = Id.of(varCtx.ID().getText());
			var v = evalCtx.getVar(varId).orElse(null);
			if (v == null) {
				return Res.error("unknown variable '"
					+ varId + "' used in init-function");
			}
			if (v.values().isEmpty()) {
				return Res.error("variable '" + varId + "' was not initialized yet");
			}
			return Res.ok(v.values().getFirst());
		}

		var func = evalCtx.getFunc(funcId).orElse(null);
		if (func == null)
			return Res.error("unknown function: " + funcId);

		// evaluate arguments & call function
		var args = new ArrayList<Cell>();
		for (var argCtx : ctx.eqn()) {
			var argRes = visit(argCtx);
			if (argRes.isError())
				return argRes;
			args.add(argRes.value());
		}
		return func.apply(args);
	}
}
