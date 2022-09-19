package org.openlca.proto.io.output;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.stream.IntStream;

public class WriterTest {

	final static UnitGroup UNIT_GROUP = UnitGroup.of("unitGroup");
	final static FlowProperty FLOW_PROPERTY = FlowProperty.of(
		"flowProperty", UNIT_GROUP);
	final static Flow FLOW = Flow.of("flow", FlowType.PRODUCT_FLOW,
		FLOW_PROPERTY);
	final static Process PROCESS = Process.of("process", FLOW);
	final static ImpactMethod IMPACT_METHOD = ImpactMethod.of("method");


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
		for (var i = 0; i < 3; i++) {
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

	@Test
	public void testWriteFlowResults() {
		var result = new Result();
		var config = new WriterConfig(null, null);

		IntStream.range(0, 3)
			.mapToObj((index) -> FlowResult.outputOf(
				Flow.of("flow" + index, FlowType.PRODUCT_FLOW, FLOW_PROPERTY),
					42.0))
			.forEach(result.flowResults::add);

		var proto = new ResultWriter(config).write(result);
		for (var i = 0; i < 3; i++) {
			var flow = proto.getFlowResults(i).getFlow();
			assertEquals("flow" + i, flow.getName());
			assertEquals(FlowType.PRODUCT_FLOW.name(),
				flow.getFlowType().getValueDescriptor().getName());
			assertEquals(42.0, proto.getFlowResults(i).getAmount(), 1e-10);
		}
	}

	@Test
	public void testWriteResult() {
		var result = new Result();
		var config = new WriterConfig(null, null);

		result.impactMethod = IMPACT_METHOD;
		result.productSystem = ProductSystem.of(PROCESS);

		var proto = new ResultWriter(config).write(result);
		assertEquals("method", proto.getImpactMethod().getName());
		assertEquals("process", proto.getProductSystem().getName());
	}

}
