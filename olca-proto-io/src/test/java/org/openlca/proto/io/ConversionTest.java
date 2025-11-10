package org.openlca.proto.io;

import static org.junit.Assert.assertEquals;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.store.EntityStore;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.proto.io.input.ProcessReader;
import org.openlca.proto.io.output.ProcessWriter;
import org.openlca.proto.io.output.WriterConfig;

@RunWith(Theories.class)
public class ConversionTest {

	@DataPoint
	public static EntityStore db = Tests.db();

	@DataPoint
	public static EntityStore mem = InMemoryStore.create();

	@Theory
	public void testConversion(EntityStore store) {

		var units = store.insert(UnitGroup.of("Mass units", "kg"));
		var property = store.insert(FlowProperty.of("Mass", units));
		var flow = store.insert(Flow.product("Steel", property));
		var process = store.insert(Process.of("Steel production", flow));

		var config = WriterConfig.of(store);
		var proto = new ProcessWriter(config).write(process);
		var resolver = EntityResolver.of(store);
		var copy = new ProcessReader(resolver).read(proto);

		assertEquals(process.refId, copy.refId);
		assertEquals(process.name, copy.name);
		assertEquals(process.processType, copy.processType);
		var qRef = copy.quantitativeReference;
		assertEquals(1, qRef.amount, 1e-16);
		assertEquals(flow, qRef.flow);
		assertEquals(units.referenceUnit, qRef.unit);
		assertEquals(property, qRef.flowPropertyFactor.flowProperty);
	}
}
