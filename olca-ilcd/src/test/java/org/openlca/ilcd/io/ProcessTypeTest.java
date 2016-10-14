package org.openlca.ilcd.io;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.descriptors.ProcessDescriptor;
import org.openlca.ilcd.processes.Process;

public class ProcessTypeTest {

	@Test
	public void testReferenceType() throws Exception {
		with(p -> {
			Assert.assertEquals(QuantitativeReferenceType.REFERENCE_FLOWS,
					p.processInfo.quantitativeReference.type);
		});
	}

	@Test
	public void testProcessType() throws Exception {
		with(p -> {
			Assert.assertEquals(ProcessType.UNIT_PROCESS,
					p.modelling.method.processType);
		});
	}

	@Test
	public void testDescriptorType() throws Exception {
		try (InputStream xml = getClass()
				.getResourceAsStream("sapi_sample_process_list.xml")) {
			DescriptorList list = JAXB.unmarshal(xml, DescriptorList.class);
			ProcessDescriptor p = (ProcessDescriptor) list.descriptors.get(0);
			Assert.assertEquals(ProcessType.LCI_RESULT, p.type);
		}
	}

	private void with(Consumer<Process> fn) throws Exception {
		try (InputStream xml = getClass()
				.getResourceAsStream("sdk_sample_process.xml")) {
			Process p = JAXB.unmarshal(xml, Process.class);
			fn.accept(p);
		}
	}

}
