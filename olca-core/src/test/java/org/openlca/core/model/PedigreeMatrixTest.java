package org.openlca.core.model;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class PedigreeMatrixTest {

	@Test(expected = IllegalArgumentException.class)
	public void testStartsWithBrackets() {
		String s = "..";
		PedigreeMatrix.fromString(s);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEndsWithBrackets() {
		String s = "(..";
		PedigreeMatrix.fromString(s);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyString() {
		String s = "";
		PedigreeMatrix.fromString(s);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFiveElements() {
		String s = "(1;2;3)";
		PedigreeMatrix.fromString(s);
	}

	@Test
	public void testValues() {
		String s = "(1;2;3;4;5)";
		Map<PedigreeMatrixRow, Integer> vals = PedigreeMatrix.fromString(s);
		assertEquals(1, (int) vals.get(PedigreeMatrixRow.RELIABILITY));
		assertEquals(2, (int) vals.get(PedigreeMatrixRow.COMPLETENESS));
		assertEquals(3, (int) vals.get(PedigreeMatrixRow.TIME));
		assertEquals(4, (int) vals.get(PedigreeMatrixRow.GEOGRAPHY));
		assertEquals(5, (int) vals.get(PedigreeMatrixRow.TECHNOLOGY));
	}

	@Test
	public void testReadPartlyEmpty() {
		String s = "(n.a.;n.a.;n.a.;4;5)";
		Map<PedigreeMatrixRow, Integer> vals = PedigreeMatrix.fromString(s);
		assertEquals(2, vals.size());
		assertEquals(4, (int) vals.get(PedigreeMatrixRow.GEOGRAPHY));
		assertEquals(5, (int) vals.get(PedigreeMatrixRow.TECHNOLOGY));
	}

	@Test
	public void testReadEmpty() {
		String s = "(n.a.;n.a.;n.a.;n.a.;n.a.)";
		Map<PedigreeMatrixRow, Integer> vals = PedigreeMatrix.fromString(s);
		assertEquals(0, vals.size());
	}

	@Test
	public void testReadWrite() {
		String[] defs = { "(1;2;3;4;5)", "(4;5;3;4;5)", "(4;5;n.a.;4;n.a.)",
				"(n.a.;n.a.;n.a.;n.a.;n.a.)" };
		for (String def : defs) {
			Map<PedigreeMatrixRow, Integer> vals = PedigreeMatrix
					.fromString(def);
			String copy = PedigreeMatrix.toString(vals);
			assertEquals(def, copy);
		}
	}
}
