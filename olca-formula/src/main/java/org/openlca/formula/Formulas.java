package org.openlca.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Formulas {

	private Formulas() {
	}

	/**
	 * Get the variables from the given expression. A variable in an expression is
	 * an identifier that is not followed by an open parenthesis (which is a
	 * function call instead).
	 */
	public static List<String> getVariables(String expression) {
		var tokens = Lexer.lex(expression);
		if (tokens.isEmpty())
			return Collections.emptyList();
		var variables = new ArrayList<String>();
		for (int i = 0; i < tokens.size(); i++) {
			var token = tokens.get(i);
			if (token.type != TokenType.IDENTIFIER)
				continue;
			if (i < (tokens.size() - 1)) {
				var next = tokens.get(i + 1);
				if (next.type == TokenType.PAREN_OPEN)
					continue;
			}
			variables.add(token.value);
		}
		return variables;
	}

	public static String renameVariable(
			String expression, String oldName, String newName) {
		if (oldName == null || newName == null)
			return expression;
		var tokens = Lexer.lex(expression);
		if (tokens.isEmpty())
			return "";
		var renamed = new ArrayList<Token>();
		var old = oldName.trim().toLowerCase();
		boolean wasRenamed = false;
		for (int i = 0; i < tokens.size(); i++) {

			var token = tokens.get(i);

			// only identifiers
			if (token.type != TokenType.IDENTIFIER
					|| token.value == null) {
				renamed.add(token);
				continue;
			}

			// check that this is not a function call
			if (i < tokens.size() - 1) {
				var next = tokens.get(i + 1);
				if (next.type == TokenType.PAREN_OPEN) {
					renamed.add(token);
					continue;
				}
			}

			// rename when the name matches
			var v = token.value.trim().toLowerCase();
			if (old.equals(v)) {
				renamed.add(Token.of(TokenType.IDENTIFIER, newName));
				wasRenamed = true;
			} else {
				renamed.add(token);
			}
		}

		return wasRenamed ? format(renamed) : expression;
	}

	public static String format(String expression) {
		var tokens = Lexer.lex(expression);
		if (tokens.isEmpty())
			return "";
		return format(tokens);
	}

	/**
	 * Currently we just put all operators except `^` between spaces. We can improve
	 * this with better support for unary operators. But for this we may first want
	 * to parse things into an abstract syntax tree.
	 */
	private static String format(List<Token> tokens) {
		if (tokens == null || tokens.isEmpty())
			return "";
		var buff = new StringBuilder();
		for (Token token : tokens) {
			if (token.isEOF())
				break;
			if (token.isError() || token.value == null)
				continue;
			if (token.type == TokenType.OPERATOR
					&& !"^".equals(token.value)) {
				buff.append(' ')
						.append(token.value)
						.append(' ');
				continue;
			}
			buff.append(token.value);
		}
		return buff.toString();
	}
}
