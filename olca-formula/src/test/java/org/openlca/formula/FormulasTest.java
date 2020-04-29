package org.openlca.formula;

import java.util.List;
import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;

public class FormulasTest {

	@Test
	public void testGetVariables() {
		BiConsumer<String, List<String>> check = (expression, vars) -> {
			List<String> variables = Formulas.getVariables(expression);
			Assert.assertEquals(variables.size(), vars.size());
			for (var v : vars) {
				Assert.assertTrue(variables.contains(v));
			}
		};
		check.accept("", List.of());
		check.accept("a", List.of("a"));
		check.accept("42 * a", List.of("a"));
		check.accept("a * b", List.of("a", "b"));
		check.accept("(a1 * b2)^c3", List.of("a1", "b2", "c3"));
	}

	@Test
	public void testFormat() {
		Assert.assertEquals("", Formulas.format("    "));
		Assert.assertEquals("()", Formulas.format(" (  )  "));
		Assert.assertEquals("1 + 1", Formulas.format("1+1"));
		Assert.assertEquals("(1 + 1)^2", Formulas.format("( 1+1 ) ^ 2"));
	}

	@Test
	public void testRename() {
		Assert.assertEquals("b",
				Formulas.renameVariable("a", "a", "b"));
		Assert.assertEquals("c + aabb",
				Formulas.renameVariable("aa + aabb", "aa", "c"));
		// never rename functions
		Assert.assertEquals("sin(pi)",
				Formulas.renameVariable("sin(pi)", "sin", "cos"));
	}

}
