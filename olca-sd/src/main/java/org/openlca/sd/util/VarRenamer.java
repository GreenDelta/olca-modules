package org.openlca.sd.util;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.openlca.commons.Res;
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
		if (oldName.equals(newName))
			return Res.ok();

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
			case EqnCell eqn -> {
				var renamed = renameInEqn(eqn.value());
				yield renamed.equals(eqn.value()) ? cell : new EqnCell(renamed);
			}
			case LookupEqnCell leqn -> {
				var renamed = renameInEqn(leqn.eqn());
				yield renamed.equals(leqn.eqn())
					? cell
					: new LookupEqnCell(renamed, leqn.func());
			}
			case TensorEqnCell teqn -> {
				var renamedEqn = renameInCell(teqn.eqn());
				renameInTensor(teqn.tensor());
				yield renamedEqn != teqn.eqn()
					? new TensorEqnCell(renamedEqn, teqn.tensor())
					: cell;
			}
			case NonNegativeCell nn -> {
				var renamed = renameInCell(nn.value());
				yield renamed != nn.value()
					? new NonNegativeCell(renamed)
					: cell;
			}
			case TensorCell tc -> {
				renameInTensor(tc.value());
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
		var oldName = variable.name();
		try {
			var lexer = new EqnLexer(CharStreams.fromString(eqn));
			var tokens = new CommonTokenStream(lexer);
			var parser = new EqnParser(tokens);
			var tree = parser.eqn();

			// collect token positions that reference the old variable name
			var positions = new ArrayList<int[]>();
			var collector = new EqnBaseListener() {
				@Override
				public void enterVar(VarContext ctx) {
					checkId(ctx.ID());
				}

				@Override
				public void enterArrayAccess(ArrayAccessContext ctx) {
					checkId(ctx.ID());
				}

				private void checkId(org.antlr.v4.runtime.tree.TerminalNode node) {
					if (node == null)
						return;
					var id = Id.of(node.getText());
					if (oldName.equals(id)) {
						positions.add(new int[]{
							node.getSymbol().getStartIndex(),
							node.getSymbol().getStopIndex() + 1
						});
					}
				}
			};
			ParseTreeWalker.DEFAULT.walk(collector, tree);

			if (positions.isEmpty())
				return eqn;

			var replacement = eqnNameOf(newName);
			var result = new StringBuilder();
			int pos = 0;
			for (var r : positions) {
				result.append(eqn, pos, r[0]);
				result.append(replacement);
				pos = r[1];
			}
			result.append(eqn, pos, eqn.length());
			return result.toString();
		} catch (Exception e) {
			return eqn;
		}
	}

	private static String eqnNameOf(Id id) {
		var label = id.label().strip();
		if (label.startsWith("\"") && label.endsWith("\""))
			return label;
		if (label.matches("[a-zA-Z_$][a-zA-Z_$0-9]*"))
			return label;
		return "\"" + label + "\"";
	}
}
