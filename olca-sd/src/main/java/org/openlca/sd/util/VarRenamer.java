package org.openlca.sd.util;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.sd.eqn.generated.EqnBaseListener;
import org.openlca.sd.eqn.generated.EqnParser;
import org.openlca.sd.eqn.generated.EqnParser.ArrayAccessContext;
import org.openlca.sd.eqn.generated.EqnParser.VarContext;
import org.openlca.sd.eqn.generated.EqnLexer;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.SdModel;
import org.openlca.sd.model.Stock;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.Var;
import org.openlca.sd.model.VarBinding;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.EqnCell;
import org.openlca.sd.model.cells.LookupEqnCell;
import org.openlca.sd.model.cells.NonNegativeCell;
import org.openlca.sd.model.cells.TensorCell;
import org.openlca.sd.model.cells.TensorEqnCell;

/// Renames a variable in the model. This means that the identifier of the
/// variable is renamed in all equations. If the variable is a flow, it is also
/// renamed in the respective inputs or outputs of stocks.
public class VarRenamer {

	private final SdModel model;
	private final Var variable;
	private final Id newName;

	private VarRenamer(SdModel model, Var variable, Id newName) {
		this.model = model;
		this.variable = variable;
		this.newName = newName;
	}

	public static Res<Void> rename(SdModel model, Var variable, Id newName) {
		if (model == null || variable == null || newName == null) {
			return Res.error(
				"A model, variable to rename, and the new name must be provided");
		}
		return new VarRenamer(model, variable, newName).doIt();
	}

	private Res<Void> doIt() {
		if (variable.name() == null) {
			variable.setName(newName);
			return Res.ok();
		}

		var oldName = variable.name();
		if (oldName.equals(newName)) {
			return Res.ok();
		}

		// check that the new name is not already used
		for (var v : model.vars()) {
			if (v != variable && newName.equals(v.name())) {
				return Res.error(
					"A variable with the name '" + newName + "' already exists");
			}
		}

		// rename references in all variable equations
		for (var v : model.vars()) {
			var cell = renameInCell(v.def());
			if (cell != v.def()) {
				v.setDef(cell);
			}
		}

		// rename references in stock in-flows and out-flows
		for (var v : model.vars()) {
			if (v instanceof Stock stock) {
				renameInFlowList(stock.inFlows());
				renameInFlowList(stock.outFlows());
			}
		}

		// update the positions map
		var pos = model.positions().remove(oldName);
		if (pos != null) {
			model.positions().put(newName, pos);
		}

		// update LCA bindings
		for (var sb : model.lca().systemBindings()) {
			if (oldName.equals(sb.amountVar())) {
				sb.setAmountVar(newName);
			}
			var bindings = sb.varBindings();
			for (int i = 0; i < bindings.size(); i++) {
				var vb = bindings.get(i);
				if (oldName.equals(vb.varId())) {
					bindings.set(i,
						new VarBinding(newName, vb.parameter(), vb.context()));
				}
			}
		}

		// finally, rename the variable itself
		variable.setName(newName);
		return Res.ok();
	}

	private void renameInFlowList(List<Id> flows) {
		var oldName = variable.name();
		for (int i = 0; i < flows.size(); i++) {
			if (oldName.equals(flows.get(i))) {
				flows.set(i, newName);
			}
		}
	}

	private Cell renameInCell(Cell cell) {
		if (cell == null)
			return null;
		return switch (cell) {

			case EqnCell c -> {
				var renamed = renameInEqn(c.value());
				yield renamed.equals(c.value()) ? cell : new EqnCell(renamed);
			}

			case LookupEqnCell c -> {
				var renamed = renameInEqn(c.eqn());
				yield renamed.equals(c.eqn())
					? cell
					: new LookupEqnCell(renamed, c.func());
			}

			case TensorEqnCell c -> {
				var renamed = renameInCell(c.eqn());
				renameInTensor(c.tensor());
				yield renamed != c.eqn()
					? new TensorEqnCell(renamed, c.tensor())
					: cell;
			}

			case NonNegativeCell c -> {
				var renamed = renameInCell(c.value());
				yield renamed != c.value()
					? new NonNegativeCell(renamed)
					: cell;
			}

			case TensorCell c -> {
				renameInTensor(c.value());
				yield cell;
			}

			default -> cell;
		};
	}

	private void renameInTensor(Tensor tensor) {
		for (int i = 0; i < tensor.size(); i++) {
			var cell = tensor.get(i);
			var renamed = renameInCell(cell);
			if (renamed != cell) {
				tensor.set(i, renamed);
			}
		}
	}

	private String renameInEqn(String eqn) {
		var posRes = PosCollector.findIn(eqn, variable.name());
		if (posRes.isError()) {
			return eqn;
		}
		var positions = posRes.value();
		if (positions.isEmpty()) {
			return eqn;
		}

		var buffer = new StringBuilder();
		int pos = 0;
		for (var p : positions) {
			buffer.append(eqn, pos, p.start);
			buffer.append(newName.value());
			pos = p.end;
		}
		buffer.append(eqn, pos, eqn.length());
		return buffer.toString();
	}

	private record Pos(int start, int end) {

		static Pos of(TerminalNode node) {
			var token = node.getSymbol();
			int start = token.getStartIndex();
			int end = token.getStopIndex() + 1;
			return new Pos(start, end);
		}
	}

	private static class PosCollector extends EqnBaseListener {

		private final Id name;
		private List<Pos> pos;

		PosCollector(Id name) {
			this.name = name;
		}

		static Res<List<Pos>> findIn(String eqn, Id name) {
			if (Strings.isBlank(eqn)) {
				return Res.ok(List.of());
			}
			try {
				var lexer = new EqnLexer(CharStreams.fromString(eqn));
				var tokens = new CommonTokenStream(lexer);
				var parser = new EqnParser(tokens);
				var collector = new PosCollector(name);
				ParseTreeWalker.DEFAULT.walk(collector,  parser.eqn());
				return collector.pos != null
					? Res.ok(collector.pos)
					: Res.ok(List.of());
			} catch (Exception e) {
				return Res.error("Failed to parse equation " + eqn, e);
			}
		}

		@Override
		public void enterVar(VarContext ctx) {
			push(ctx.ID());
		}

		@Override
		public void enterArrayAccess(ArrayAccessContext ctx) {
			push(ctx.ID());
		}

		private void push(TerminalNode node) {
			if (node == null) return;
			var id = Id.of(node.getText());
			if (!name.equals(id)) return;
			if (pos == null) {
				pos = new ArrayList<>(2);
			}
			pos.add(Pos.of(node));
		}
	}

}
