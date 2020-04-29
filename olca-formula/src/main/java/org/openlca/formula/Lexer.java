package org.openlca.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

interface State extends Supplier<State> {
}

class Lexer {

	private final String input;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int pos = 0;

	Lexer(String input) {
		this.input = input;
	}

	static List<Token> lex(String input) {
		return new Lexer(input).lex();
	}

	List<Token> lex() {
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

			if (isOperatorPart(next))
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
				pos++;
				start = pos;
				continue;
			}
		}
		tokens.add(Token.eof());
		return null;
	}

	private State lexNumber() {
		acceptDigits();

		if (acceptOneOf('.')) {
			if (!acceptDigits()) {
				tokens.add(Token.error(
					"no digits after decimal separator @" + start));
				return null;
			}
		}

		if (acceptOneOf('e', 'E')) {
			acceptOneOf('+', '-');
			if (!acceptDigits()) {
				tokens.add(Token.error(
					"no digits after exponential separator @" + start));
				return null;
			}
		}

		emit(TokenType.NUMBER);
		return this::lexText;
	}

	private State lexIdentifier() {
		while (this.hasNext()) {
			var next = this.peek();
			if (Character.isJavaIdentifierPart(next)) {
				pos++;
				continue;
			}
			break;
		}
		emit(TokenType.IDENTIFIER);
		return this::lexText;
	}

	private State lexOperator() {
		while (this.hasNext()) {
			var next = this.peek();
			if (isOperatorPart(next)) {
				pos++;
				continue;
			}
			break;
		}
		emit(TokenType.OPERATOR);
		return this::lexText;
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

	/**
	 * Moves the reading position forward by one if the next character is one of the
	 * given characters. Returns true if this was the case.
	 */
	private boolean acceptOneOf(char... cs) {
		if (!hasNext())
			return false;
		char next = peek();
		for (char c : cs) {
			if (c == next) {
				pos++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Moves the reading position forward if next characters are a sequence of
	 * digits. Returns true there was at least one digit as next character.
	 */
	private boolean acceptDigits() {
		var found = false;
		while (hasNext()) {
			char next = this.peek();
			if (Character.isDigit(next)) {
				pos++;
				found = true;
				continue;
			}
			break;
		}
		return found;
	}

	private boolean hasNext() {
		return pos < input.length();
	}

	private char peek() {
		return input.charAt(pos);
	}

	private boolean isOperatorPart(char c) {
		return c == '+'
				|| c == '-'
				|| c == '*'
				|| c == '/'
				|| c == '^'
				|| c == '='
				|| c == '<'
				|| c == '>'
				|| c == '&'
				|| c == '|'
				|| c == '!';
	}
}
