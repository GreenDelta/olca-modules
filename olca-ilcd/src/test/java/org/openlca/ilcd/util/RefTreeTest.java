package org.openlca.ilcd.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.io.ProcessSampleTest;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;

public class RefTreeTest {

	@Test
	public void testNoRef() {
		Process p = new Process();
		RefTree tree = RefTree.create(p);
		assertTrue(tree.root.childs.isEmpty());
		assertTrue(tree.getRefs().isEmpty());
	}

	@Test
	public void testFlowRef() {
		Process p = new Process();
		Exchange e = new Exchange();
		p.exchanges.add(e);
		Ref flowRef = new Ref();
		flowRef.uuid = "123";
		e.flow = flowRef;
		RefTree tree = RefTree.create(p);
		assertEquals(1, tree.root.childs.size());
		Ref ref = tree.getRefs().get(0);
		assertEquals("123", ref.uuid);
	}

	@Test
	public void testProcessSample() {
		InputStream is = ProcessSampleTest.class
				.getResourceAsStream("sdk_sample_process.xml");
		Process p = JAXB.unmarshal(is, Process.class);
		RefTree tree = RefTree.create(p);
		assertTrue(tree.getRefs().size() > 2);
		int lciaResultCount = 0;
		for (Ref ref : tree.getRefs()) {
			if (ref.type == DataSetType.LCIA_METHOD) {
				assertTrue(ref.isValid());
				lciaResultCount++;
			}
		}
		assertEquals(1, lciaResultCount); // two references but with same uuid
											// and version
	}

	@Test
	public void testFlowSample() {
		InputStream is = ProcessSampleTest.class
				.getResourceAsStream("sdk_sample_flow.xml");
		Flow p = JAXB.unmarshal(is, Flow.class);
		RefTree tree = RefTree.create(p);
		assertTrue(tree.getRefs().size() > 2);
	}

}
