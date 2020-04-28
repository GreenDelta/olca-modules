package org.openlca.formula.ast;

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
}
