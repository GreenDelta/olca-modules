package org.openlca.sd.eqn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.openlca.commons.Res;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.eqn.generated.EqnBaseListener;
import org.openlca.sd.eqn.generated.EqnLexer;
import org.openlca.sd.eqn.generated.EqnParser;
import org.openlca.sd.eqn.generated.EqnParser.ArrayAccessContext;
import org.openlca.sd.eqn.generated.EqnParser.VarContext;
import org.openlca.sd.model.Id;

public class Interpreter {

	private final EvalContext ctx;

	private Interpreter(EvalContext ctx) {
		this.ctx = ctx;
	}

	public static Interpreter of(EvalContext ctx) {
		return ctx != null
			? new Interpreter(ctx)
			: new Interpreter(new EvalContext());
	}

	public EvalContext context() {
		return ctx;
	}

	public Res<Cell> eval(String expression) {
		if (Id.isNil(expression))
			return Res.error("empty expression provided");
		var lexer = new EqnLexer(CharStreams.fromString(expression));
		var tokens = new CommonTokenStream(lexer);
		var parser = new EqnParser(tokens);
		return new EvalVisitor(ctx).visit(parser.eqn());
	}

	public static Res<List<Id>> varsOf(String expression) {
		if (Id.isNil(expression))
			return Res.error("provided expression is empty");

		// predefined no-parameter functions that are not variables
		var fns = Set.of(
			Id.of("INF"),
			Id.of("PI"),
			Id.of("DT"),
			Id.of("STARTTIME"),
			Id.of("STOPTIME"),
			Id.of("TIME"),
			Id.of("SELF")
		);

		try {
			var lexer = new EqnLexer(CharStreams.fromString(expression));
			var tokens = new CommonTokenStream(lexer);
			var parser = new EqnParser(tokens);
			var tree = parser.eqn();

			var vars = new HashSet<Id>();
			Consumer<TerminalNode> pushVar = (node) -> {
				if (node == null)
					return;
				var v = Id.of(node.getText());
				if (!v.isNil() && !fns.contains(v)) {
					vars.add(v);
				}
			};

			var collector = new EqnBaseListener() {
				@Override
				public void enterVar(VarContext ctx) {
					pushVar.accept(ctx.ID());
				}

				@Override
				public void enterArrayAccess(ArrayAccessContext ctx) {
					pushVar.accept(ctx.ID());
				}
			};

			ParseTreeWalker.DEFAULT.walk(collector, tree);
			return Res.ok(List.copyOf(vars));
		} catch (Exception e) {
			return Res.error("failed to collect vars from expression", e);
		}
	}

}
