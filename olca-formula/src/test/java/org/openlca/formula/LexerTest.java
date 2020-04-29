package org.openlca.formula;

import org.junit.Assert;
import org.junit.Test;

public class LexerTest {

	@Test
	public void testLexEmpty() {
		var tokens = Lexer.lex("  ");
		Assert.assertEquals(1, tokens.size());
		Assert.assertTrue(tokens.get(0).isEOF());
	}

	@Test
	public void testLexParens() {
		var tokens = Lexer.lex(" ( ) ");
		Assert.assertEquals(3, tokens.size());
		Assert.assertEquals(tokens.get(0).type, TokenType.PAREN_OPEN);
		Assert.assertEquals(tokens.get(1).type, TokenType.PAREN_CLOSE);
		Assert.assertTrue(tokens.get(2).isEOF());
	}

	@Test
	public void testLexIdentifiers() {
		String[] ids = {
			"a",
            "abcdefghijklmnopqrstuvwxyz",
            "abcdefghijklmnopqrstuvwxyz".toUpperCase(),
            "a0123456789",
		};
		for (var id : ids) {
			var tokens = Lexer.lex(id);
			Assert.assertEquals(2, tokens.size());
			Assert.assertEquals(tokens.get(0).type, TokenType.IDENTIFIER);
			Assert.assertEquals(tokens.get(0).value, id.trim());
			Assert.assertTrue(tokens.get(1).isEOF());
		}
	}

	@Test
	public void testLexOperators() {
		String[] ops = {
			"+",
            "-",
            "*",
            "/",
            "^",
            ">",
            "<",
            "=",
            ">=",
            "<=",
            "==",
            "&",
            "|",
            "&&",
            "||",
		};
		for (var op : ops) {
			var tokens = Lexer.lex(op);
			Assert.assertEquals(2, tokens.size());
			Assert.assertEquals(tokens.get(0).type, TokenType.OPERATOR);
			Assert.assertEquals(tokens.get(0).value, op.trim());
			Assert.assertTrue(tokens.get(1).isEOF());
		}
	}


}
