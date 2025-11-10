package org.openlca.proto.io.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.IntStream;

import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.proto.ProtoFlowType;

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
		var doc = new ProcessDoc();
		IntStream.range(0, 3)
			.mapToObj((index) -> Source.of("source" + index))
			.forEach(doc.sources::add);
		process.documentation = doc;

		var config = WriterConfig.of(InMemoryStore.create());
		var proto = new ProcessWriter(config).write(process);
		for (var i = 0; i < 3; i++) {
			var name = proto.getProcessDocumentation()
				.getSources(i)
				.getName();
			assertEquals("source" + i, name);
		}
	}

	@Test
	public void testWriteDocumentation() {
		var process = new Process();
		var doc = new ProcessDoc();
		doc.technology = "technology";
		doc.copyright = true;
		var date = new GregorianCalendar(2022, Calendar.SEPTEMBER, 16);
		doc.validUntil = date.getTime();
		process.documentation = doc;
		var rev = new Review();
		rev.reviewers.add(Actor.of("actor"));
		doc.reviews.add(rev);

		var config = WriterConfig.of(InMemoryStore.create());
		var proto = new ProcessWriter(config).write(process);
		var protoDoc = proto.getProcessDocumentation();
		assertEquals("technology", protoDoc.getTechnologyDescription());
		assertTrue(protoDoc.getIsCopyrightProtected());
		assertEquals("actor", protoDoc.getReviewer().getName());
		assertEquals("2022-09-16", protoDoc.getValidUntil());
	}

	@Test
	public void testWriteFlowResults() {
		var result = new Result();
		var mass = FlowProperty.of("Mass", UnitGroup.of("Mass units"));
		IntStream.range(0, 3)
			.mapToObj(i -> FlowResult.outputOf(
				Flow.of("flow" + i, FlowType.ELEMENTARY_FLOW, mass), 42.0))
			.forEach(result.flowResults::add);

		var config = WriterConfig.of(InMemoryStore.create());
		var proto = new ResultWriter(config).write(result);
		for (var i = 0; i < 3; i++) {
			var r = proto.getFlowResults(i);
			assertEquals("flow" + i, r.getFlow().getName());
			assertEquals(ProtoFlowType.ELEMENTARY_FLOW, r.getFlow().getFlowType());
			assertEquals(42.0, r.getAmount(), 1e-10);
		}
	}

	@Test
	public void testWriteResult() {
		var result = new Result();
		result.impactMethod = ImpactMethod.of("method");
		result.productSystem = new ProductSystem();
		result.productSystem.name = "system";

		var config = WriterConfig.of(InMemoryStore.create());
		var proto = new ResultWriter(config).write(result);
		assertEquals("method", proto.getImpactMethod().getName());
		assertEquals("system", proto.getProductSystem().getName());
	}

}
