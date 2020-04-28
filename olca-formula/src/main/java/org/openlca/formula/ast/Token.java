package org.openlca.formula.ast;

public class Token {

	public final TokenType type;
	public final String value;
	
	public Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public static Token of(TokenType type, String value) {
		return new Token(type, value);
	}
	
	public static Token eof() {
		return of(TokenType.EOF, "");
	}
	
	public static Token error(String message) {
		return of(TokenType.ERROR, message);
	}
	
	public boolean isEOF() {
		return type == TokenType.EOF;
	}
	
	public boolean isError() {
		return type == TokenType.ERROR;
	}
	
	@Override
	public String toString() {
		return "Token { type: " + type 
				+ ", value: " + value + "}";
	}
}
