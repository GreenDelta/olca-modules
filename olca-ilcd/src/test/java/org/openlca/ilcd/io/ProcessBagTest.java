package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Categories;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.ilcd.util.Processes;

public class ProcessBagTest {

	private ProcessBag bag;

	@Before
	public void setUp() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"process.xml")) {
			XmlBinder binder = new XmlBinder();
			Process process = binder.fromStream(Process.class, stream);
			bag = new ProcessBag(process, "en");
		}
	}

	@Test
	public void testGetId() {
		assertEquals("76d6aaa4-37e2-40b2-994c-03292b600074", bag.getId());
	}

	@Test
	public void testGetName() {
		assertEquals("Acrylonitrile-Butadiene-Styrene granulate "
				+ "(ABS), production mix, at plant", 
				Processes.fullName(bag.getValue(), "en"));
	}

	@Test
	public void testGetSynonyms() {
		assertEquals(
				"Acrylonitrile-butadiene-styrene copolymer; "
						+ "Styrene, acrylonitrile, butadiene polymer; 2-Propenenitrile, "
						+ "polymer with 1,3-butadiene and ethenylbenzene; Acrylonitrile, "
						+ "polymer with 1,3-butadiene and styrene",
				bag.getSynonyms());
	}

	@Test
	public void testCategoryPath() {
		String[] path = Categories.getPath(bag.getValue());
		assertEquals(path.length, 2);
		assertEquals("Materials production", path[0]);
		assertEquals("Plastics", path[1]);
	}

	@Test
	public void testGetTime() {
		Time time = bag.getTime();
		assertEquals(1996, time.referenceYear.intValue());
	}

	@Test
	public void testGetGeography() {
		Geography geography = bag.getGeography();
		assertEquals("RER", geography.location.code);
	}

	@Test
	public void testGetReferenceFlowIds() {
		List<Integer> refs = bag.getReferenceFlowIds();
		assertTrue(refs.size() == 1);
		assertEquals(Integer.valueOf(56), refs.get(0));
	}

	@Test
	public void testGetProcessType() {
		assertEquals(ProcessType.PARTLY_TERMINATED_SYSTEM,
				bag.getProcessType());
	}

	@Test
	public void testGetExchanges() {
		List<Exchange> exchanges = bag.getExchanges();
		assertTrue(exchanges.size() > 56);
	}

}
