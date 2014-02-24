package org.openlca.expressions;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests and demonstrates how the variables from a formula can be extracted.
 */
public class VariableRetrievalTest {

	@Test
	public void testGetSingle() throws Exception {
		List<VariableFunction> variables = getVariables(" a ");
		Assert.assertEquals(1, variables.size());
		Assert.assertEquals("a", variables.get(0).getVariableName());
	}

	@Test
	public void testGetFromFunction() throws Exception {
		List<VariableFunction> variables = getVariables(" sin(a) ");
		Assert.assertEquals(1, variables.size());
		Assert.assertEquals("a", variables.get(0).getVariableName());
	}

	@Test
	public void testGetMultiple() throws Exception {
		List<VariableFunction> variables = getVariables(" a + b / c ^ 3 ");
		Assert.assertEquals(3, variables.size());
		Assert.assertEquals("a", variables.get(0).getVariableName());
		Assert.assertEquals("b", variables.get(1).getVariableName());
		Assert.assertEquals("c", variables.get(2).getVariableName());
	}

	@Test
	public void testGetMultipleFromFunctions() throws Exception {
		List<VariableFunction> variables = getVariables(" sin(a) + cos(b / c ^ 3) ");
		Assert.assertEquals(3, variables.size());
		Assert.assertEquals("a", variables.get(0).getVariableName());
		Assert.assertEquals("b", variables.get(1).getVariableName());
		Assert.assertEquals("c", variables.get(2).getVariableName());
	}

	private List<VariableFunction> getVariables(String formula)
			throws Exception {
		FormulaParser parser = new FormulaParser(new StringReader(formula));
		parser.parse();
		return parser.getVariables();
	}
}
