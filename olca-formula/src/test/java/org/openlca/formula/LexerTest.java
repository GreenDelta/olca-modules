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

	@Test
	public void testLexNumbers() {
		String[] nums = {
				"42",
				"4.2",
				"4e2",
				"4E2",
				"4e-2",
				"4E+2",
				"4.2e2",
				"  423.2123E+42  ",
		};
		for (var num : nums) {
			var tokens = Lexer.lex(num);
			Assert.assertEquals(2, tokens.size());
			Assert.assertEquals(tokens.get(0).type, TokenType.NUMBER);
			Assert.assertEquals(tokens.get(0).value, num.trim());
			Assert.assertTrue(tokens.get(1).isEOF());
		}
	}

	@Test
	public void testCatchNumberErrors() {
		String[] nums = {
				".",
				".e",
				".1e",
				".1e-",
		};
		for (var num : nums) {
			var tokens = Lexer.lex(num);
			Assert.assertEquals(tokens.get(0).type, TokenType.ERROR);
		}
	}

	@Test
	public void testLexNumberSequence() {
		var nums = " 0 1 2 3 4 5 6 7 8 9 ";
		var tokens = Lexer.lex(nums);
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals(tokens.get(i).type, TokenType.NUMBER);
			Assert.assertEquals(tokens.get(i).value, Integer.toString(i));
		}
		Assert.assertTrue(tokens.get(10).isEOF());
	}

	@Test
	public void testLexExpression() {
		var tokens = Lexer.lex("sin( 42 * pi )");
		Assert.assertEquals(tokens.get(0).type, TokenType.IDENTIFIER);
		Assert.assertEquals(tokens.get(0).value, "sin");
		Assert.assertEquals(tokens.get(1).type, TokenType.PAREN_OPEN);
		Assert.assertEquals(tokens.get(2).type, TokenType.NUMBER);
		Assert.assertEquals(tokens.get(2).value, "42");
		Assert.assertEquals(tokens.get(3).type, TokenType.OPERATOR);
		Assert.assertEquals(tokens.get(3).value, "*");
		Assert.assertEquals(tokens.get(4).type, TokenType.IDENTIFIER);
		Assert.assertEquals(tokens.get(4).value, "pi");
		Assert.assertEquals(tokens.get(5).type, TokenType.PAREN_CLOSE);
	}
}
