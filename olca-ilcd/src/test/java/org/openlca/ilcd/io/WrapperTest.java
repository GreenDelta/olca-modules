package org.openlca.ilcd.io;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.processes.Process;

public class WrapperTest {

	@Test
	public void testExchanges() {
		InputStream in = getClass()
				.getResourceAsStream("sdk_sample_process.xml");
		Process p = JAXB.unmarshal(in, Process.class);
		Assert.assertEquals(2, p.exchanges.size());
		Assert.assertEquals(2, p.lciaResults.length);
	}

}
