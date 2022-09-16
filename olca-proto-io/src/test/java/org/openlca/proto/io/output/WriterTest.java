package org.openlca.proto.io.output;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.IntStream;

public class WriterTest {

	@Test
	public void testWriteCategoryPath() {
		var actor = Actor.of("actor");
		var path = "Some/Actor/Category";
		Category category = null;
		for (var seg : path.split("/")) {
			var next = Category.of(seg, ModelType.ACTOR);
			if (category != null) {
				next.category = category;
				category.childCategories.add(next);
			}
			category = next;
		}
		actor.category = category;

		var proto = new ActorWriter().write(actor);
		assertEquals(path, proto.getCategory());
	}

	@Test
	public void testWriteDocumentationSources() {
		var process = new Process();
		var doc = new ProcessDocumentation();
		var config = new WriterConfig(null, null);

		IntStream.range(0, 3)
			.mapToObj((index) -> Source.of("source" + index))
			.forEach(doc.sources::add);
		process.documentation = doc;

		var proto = new ProcessWriter(config).write(process);
		for (var i = 0; i < 3; i++){
			var name = proto.getProcessDocumentation().getSources(i).getName();
		assertEquals("source" + i, name);
		}
	}

	@Test
	public void testWriteDocumentation() {
		var process = new Process();
		var doc = new ProcessDocumentation();
		var config = new WriterConfig(null, null);

		doc.technology = "technology";
		doc.copyright = true;
		doc.reviewer = Actor.of("actor");
		var date = new GregorianCalendar(2022, Calendar.SEPTEMBER, 16);
		doc.validUntil = date.getTime();
		process.documentation = doc;

		var proto = new ProcessWriter(config).write(process);

		var technology = proto.getProcessDocumentation().getTechnologyDescription();
		assertEquals("technology", technology);
		var copyright = proto.getProcessDocumentation().getIsCopyrightProtected();
		assertTrue(copyright);
		var reviewer = proto.getProcessDocumentation().getReviewer();
		assertEquals("actor", reviewer.getName());
		var validUntil = proto.getProcessDocumentation().getValidUntil();
		assertEquals("2022-09-16", validUntil);
	}

}
