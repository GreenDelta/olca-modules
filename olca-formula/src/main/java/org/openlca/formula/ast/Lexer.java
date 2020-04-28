package org.openlca.formula.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

interface State extends Supplier<State> {
}

public class Lexer {

	private final String input;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int pos = 0;

	public Lexer(String input) {
		this.input = input;
	}

	public static List<Token> lex(String input) {
		return new Lexer(input).lex();
	}

	public List<Token> lex() {
		if (input == null)
			return tokens;
		State state = this::lexText;
		while (state != null) {
			state = state.get();
		}
		return tokens;
	}

	private State lexText() {
		while (hasNext()) {
			char next = peek();

			if (next == '.' || Character.isDigit(next))
				return this::lexNumber;

			if (Character.isJavaIdentifierStart(next))
				return this::lexIdentifier;

			if (isOperator(next))
				return this::lexOperator;

			if (next == '(') {
				emitSingle(TokenType.PAREN_OPEN, "(");
				continue;
			}

			if (next == ')') {
				emitSingle(TokenType.PAREN_CLOSE, ")");
				continue;
			}

			if (next == ';') {
				emitSingle(TokenType.SEPARATOR, ";");
				continue;
			}

			if (Character.isWhitespace(next)) {
				this.pos++;
				continue;
			}
		}
		tokens.add(Token.eof());
		return null;
	}

	private State lexNumber() {
		return null;
	}

	private State lexIdentifier() {
		return null;
	}

	private State lexOperator() {
		return null;
	}

	private void emit(TokenType type) {
		tokens.add(Token.of(type, input.substring(start, pos)));
		start = pos;
	}

	private void emitSingle(TokenType type, String val) {
		tokens.add(Token.of(type, val));
		pos++;
		start = pos;
	}

	private boolean hasNext() {
		return pos < input.length();
	}

	private char peek() {
		return input.charAt(pos);
	}

	private boolean isOperator(char c) {
		return c == '+'
				|| c == '-'
				|| c == '*'
				|| c == '/'
				|| c == '^'
				|| c == '='
				|| c == '<'
				|| c == '>'
				|| c == '&'
				|| c == '|';
	}
}
