package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.Id;

public class InterpreterTest {

	@Test
	public void testVarsOf() {

		// no variables
		assertTrue(varsOf("2 + 3").isEmpty());
		assertTrue(varsOf("(5 * 7) - 1").isEmpty());

		// single variable
		checkVars("x", "x");
		checkVars("a + 2", "a");
		checkVars("3 * B", "b");

		// multiple variables
		checkVars("a + b", "a", "b");
		checkVars("X * y + z", "x", "y", "z");
		checkVars("foo + bar - Baz", "foo", "bar", "baz");

		// variables in functions
		checkVars("ABS(x)", "x");
		checkVars("MAX(a, B, c)", "a", "b", "c");

		// variables in array access
		checkVars("Arr[i]", "arr");
		checkVars("matrix[x, y]", "matrix");

		// variables in nested expressions
		checkVars("a * (b + c)", "a", "b", "c");
		checkVars("(x + y) * (z - w)", "x", "y", "z", "w");

		// variables in if-then-else
		checkVars("IF a > 0 THEN b ELSE c", "a", "b", "c");
		checkVars("IF (x == 1) THEN y ELSE z", "x", "y", "z");

		// variables with underscores and numbers
		checkVars("var_1 + Var_2", "VAR_1", "var_2");
		checkVars("x1 + y2", "x1", "y2");

		// variables in power, mod, and logical expressions
		checkVars("a^b", "a", "b");
		checkVars("x MOD y", "x", "y");
		checkVars("p AND q", "p", "q");
		checkVars("foo OR bar","foo", "bar");

		// variables in negation
		checkVars("-x", "x");
		checkVars("NOT y", "y");

		// duplicates should not appear
		checkVars("a + a + b", "a", "b");
	}

	private void checkVars(String expression, String... vars) {
		var vs = varsOf(expression);
		assertEquals(vars.length, vs.size());
		for (var v : vars) {
			assertTrue(vs.contains(Id.of(v)));
		}
	}

	private List<Id> varsOf(String expression) {
		return Interpreter.varsOf(expression).orElseThrow();
	}
}

