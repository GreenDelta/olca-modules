package org.openlca.simapro.csv;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.simapro.csv.io.Event;
import org.openlca.simapro.csv.io.Parser;

public class SimpleParserTest {

	@Test
	public void testBlocks() throws Exception {
		String text = "process \n end \n method \n end \n";
		Parser parser = new Parser(new StringReader(text));
		AtomicInteger count = new AtomicInteger(0);
		parser.parse((evt, content) -> {
			if (evt == Event.START_BLOCK) {
				if (count.incrementAndGet() == 1) {
					Assert.assertEquals("process", content);
				} else {
					Assert.assertEquals("method", content);
				}
			}
		});
		parser.close();
		Assert.assertEquals(2, count.get());
	}

	@Test
	public void testBlockData() throws Exception {
		String text = "process \n data row \n\n end \n";
		Parser parser = new Parser(new StringReader(text));
		AtomicInteger count = new AtomicInteger(0);
		parser.parse((evt, content) -> {
			switch (evt) {
			case START_BLOCK:
				Assert.assertEquals("process", content);
				count.incrementAndGet();
				break;
			case DATA_ROW:
				Assert.assertEquals("data row", content);
				count.incrementAndGet();
				break;
			case END_BLOCK:
				Assert.assertEquals("process", content);
				count.incrementAndGet();
				break;
			default:
				break;
			}
		});
		parser.close();
		Assert.assertEquals(3, count.get());
	}

	@Test
	public void testSectionData() throws Exception {
		String text = "process \n\n section \n data row \n\n end \n";
		Parser parser = new Parser(new StringReader(text));
		AtomicBoolean inBlock = new AtomicBoolean(false);
		AtomicBoolean inSection = new AtomicBoolean(false);
		AtomicBoolean dataFound = new AtomicBoolean(false);
		parser.parse((evt, content) -> {
			switch (evt) {
			case START_BLOCK:
				Assert.assertEquals("process", content);
				inBlock.set(true);
				break;
			case START_SECTION:
				Assert.assertEquals("section", content);
				inSection.set(true);
				break;
			case DATA_ROW:
				Assert.assertTrue(inBlock.get() && inSection.get());
				Assert.assertEquals("data row", content);
				dataFound.set(true);
				break;
			default:
				break;
			}
		});
		parser.close();
		Assert.assertTrue(dataFound.get());
	}
}
