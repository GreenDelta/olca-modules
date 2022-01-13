package org.openlca.text;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PhraseParserTest {

	@Test
	public void testParse() {
		var parser = new PhraseParser();

		assertArrayEquals(
			new String[]{"the", "parser", "is", "case", "insensitive"},
			parser.parse("The parser is case INSENSITIVE").toArray());

		var buffer = parser.parse("""
			The parser keeps
			the word order, but
			the parser removes
			duplicates.""");
		assertArrayEquals(
			new String[]{
				"the", "parser", "keeps", "word", "order",
				"but", "removes", "duplicates"},
			buffer.toArray());
	}

	@Test
	public void testStopWords() {
		var parser = new PhraseParser(Set.of("the", "is", "but"));

		assertArrayEquals(
			new String[]{"parser", "case", "insensitive"},
			parser.parse("The parser is case INSENSITIVE").toArray());

		var buffer = parser.parse("""
			The parser keeps
			the word order, but
			the parser removes
			duplicates.""");
		assertArrayEquals(
			new String[]{
				"parser", "keeps", "word", "order",
				"removes", "duplicates"},
			buffer.toArray());
	}

	@Test
	public void testParseInto() {
		var parser = new PhraseParser(Set.of("the", "is", "but"));
		var buffer = new WordBuffer();

		parser.parseInto(buffer, "The parser is case INSENSITIVE");
		assertArrayEquals(
			new String[]{"parser", "case", "insensitive"},
			buffer.toArray());

		parser.parseInto(buffer, """
			The parser keeps
			the word order, but
			the parser removes
			duplicates.""");
		assertArrayEquals(
			new String[]{
				"parser", "keeps", "word", "order",
				"removes", "duplicates"},
			buffer.toArray());
	}
}
