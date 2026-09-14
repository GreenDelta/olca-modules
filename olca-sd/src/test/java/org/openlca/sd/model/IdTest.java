package org.openlca.sd.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class IdTest {

	@Test
	public void testEncodedNewLine() {
		var id = Id.of("carrying\\ncapacity");
		assertEquals("carrying capacity", id.label());
		assertEquals("carrying_capacity", id.value());
	}

	@Test
	public void testCanonicalForms() {
		var expected = Id.of("a_var");
		var forms = new String[] {
			"a Var",
			"a_Var",
			"\"a Var\"",
			"a\\nVar",
			"A VAR",
			"  a   Var  ",
		};
		for (var form : forms) {
			var id = Id.of(form);
			assertEquals(form, expected, id);
			assertEquals(form, "a_var", id.value());
			assertEquals(form, id.hashCode(), expected.hashCode());
		}
	}

	@Test
	public void testLabelsOfCanonicalForms() {
		assertEquals("a Var", Id.of("a Var").label());
		assertEquals("a_Var", Id.of("a_Var").label());
		assertEquals("a Var", Id.of("\"a Var\"").label());
		assertEquals("a Var", Id.of("a\\nVar").label());
	}

	@Test
	public void testConsecutiveSeparators() {
		for (var form : new String[] {
			"a\\n\\nb", "a\\n b", "a \\nb", "a  b" }) {
			var id = Id.of(form);
			assertEquals(form, "a_b", id.value());
			assertEquals(form, "a b", id.label());
			assertEquals(form, Id.of("a b"), id);
		}
	}

	@Test
	public void testLeadingAndTrailingWhitespace() {
		var id = Id.of("  carrying capacity  ");
		assertEquals("carrying capacity", id.label());
		assertEquals("carrying_capacity", id.value());
	}

	@Test
	public void testCaseIsPreservedInLabel() {
		var id = Id.of("Carrying Capacity");
		assertEquals("Carrying Capacity", id.label());
		assertEquals("carrying_capacity", id.value());
		assertEquals(Id.of("carrying_capacity"), id);
	}

	@Test
	public void testOnlyNewLineEscapeIsDecoded() {
		// other escape-like sequences are kept as they are
		var id = Id.of("a\\tb");
		assertEquals("a\\tb", id.label());
		assertEquals("a\\tb", id.value());
	}

	@Test
	public void testNilId() {
		assertTrue(Id.isNil(null));
		assertTrue(Id.isNil(""));
		assertTrue(Id.isNil("  \t "));
		for (var s : new String[] { null, "", "  \t " }) {
			var id = Id.of(s);
			assertTrue(id.isNil());
			assertEquals("", id.value());
		}
	}

	@Test
	public void testMatches() {
		var id = Id.of("carrying\\ncapacity");
		assertTrue(id.matches("carrying capacity"));
		assertTrue(id.matches("carrying_capacity"));
		assertTrue(id.matches("\"carrying capacity\""));
		assertFalse(id.matches("carrying-capacity"));
	}
}
