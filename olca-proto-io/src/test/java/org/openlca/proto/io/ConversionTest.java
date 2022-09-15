package org.openlca.proto.io;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.io.InMemoryResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.proto.io.input.ProcessReader;
import org.openlca.proto.io.output.ProcessWriter;
import org.openlca.proto.io.output.WriterConfig;

public class ConversionTest {

	@Test
	public void testInMemoryConversion() {
		var store = InMemoryStore.create();

		var units = store.insert(UnitGroup.of("Mass units", "kg"));
		var property = store.insert(FlowProperty.of("Mass", units));
		var flow = store.insert(Flow.product("Steel", property));
		var process = store.insert(Process.of("Steel production", flow));

		var config = WriterConfig.of(store);
		var proto = new ProcessWriter(config).write(process);
		var resolver = new InMemoryResolver(store);
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
