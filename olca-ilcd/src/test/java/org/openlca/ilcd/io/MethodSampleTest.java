package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.methods.AreaOfProtection;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.LCIAMethod;

public class MethodSampleTest {

	@Test
	public void testDataSetInfo() throws Exception {
		with(m -> {
			DataSetInfo info = m.methodInfo.dataSetInfo;
			assertEquals("00000000-0000-0000-0000-000000000000", info.uuid);
			assertEquals(2, info.name.size());
			assertEquals(2, info.methods.size());
			assertEquals("ILCD", info.classifications.get(0).name);
			assertEquals("Acidification", info.impactCategories.get(1));
			assertEquals(AreaOfProtection.NATURAL_RESOURCES, info.areasOfProtection.get(0));
			assertEquals(2, info.comment.size());
			assertEquals(2, info.externalDocs.size());
		});
	}

	private void with(Consumer<LCIAMethod> fn) throws Exception {
		try (InputStream in = getClass()
				.getResourceAsStream("sdk_sample_lciamethod.xml")) {
			LCIAMethod m = JAXB.unmarshal(in, LCIAMethod.class);
			fn.accept(m);
		}
	}

}
