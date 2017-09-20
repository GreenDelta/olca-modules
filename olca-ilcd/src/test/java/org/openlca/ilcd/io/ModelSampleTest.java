package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.openlca.ilcd.models.Model;

public class ModelSampleTest {

	@Test
	public void testDataSetInfo() throws Exception {
		with(m -> {
			assertEquals("10062015-184a-41b8-8fa6-49e999cbd101", m.getUUID());
		});
	}

	private void with(Consumer<Model> fn) throws Exception {
		try (InputStream in = getClass()
				.getResourceAsStream("eilcd_sample_model.xml")) {
			Model p = JAXB.unmarshal(in, Model.class);
			fn.accept(p);
		}
	}
}
