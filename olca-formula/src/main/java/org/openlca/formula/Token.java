package org.openlca.formula;

class Token {

	final TokenType type;
	final String value;

	Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}

	static Token of(TokenType type, String value) {
		return new Token(type, value);
	}

	static Token eof() {
		return of(TokenType.EOF, "");
	}

	static Token error(String message) {
		return of(TokenType.ERROR, message);
	}

	boolean isEOF() {
		return type == TokenType.EOF;
	}

	boolean isError() {
		return type == TokenType.ERROR;
	}

	@Override
	public String toString() {
		return "Token { type: " + type
				+ ", value: " + value + "}";
	}
}
