package org.openlca.sd.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RoundTripTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testExampleRoundTrip() throws IOException {
		var stream = getClass().getResourceAsStream("/org/openlca/sd/xmile/example.xml");
		assertNotNull(stream);

		SdModel model = SdModel.readFrom(stream).orElseThrow();

		File file = folder.newFile("output.xml");
		model.writeTo(file).orElseThrow();

		// Read it back
		SdModel model2 = SdModel.readFrom(Files.newInputStream(file.toPath())).orElseThrow();

		// Basic checks
		assertEquals(model.time().start(), model2.time().start(), 1e-6);
		assertEquals(model.time().end(), model2.time().end(), 1e-6);
		assertEquals(model.time().dt(), model2.time().dt(), 1e-6);
		assertEquals(model.vars().stream().filter(v -> v instanceof Auxil).count(),
				model2.vars().stream().filter(v -> v instanceof Auxil).count());
		assertEquals(model.vars().stream().filter(v -> v instanceof Rate).count(),
				model2.vars().stream().filter(v -> v instanceof Rate).count());
		assertEquals(model.vars().stream().filter(v -> v instanceof Stock).count(),
				model2.vars().stream().filter(v -> v instanceof Stock).count());
	}

	@Test
	public void testArraysRoundTrip() throws IOException {
		var stream = getClass().getResourceAsStream("/org/openlca/sd/xmile/arrays.xml");
		assertNotNull(stream);

		SdModel model = SdModel.readFrom(stream).orElseThrow();

		File file = folder.newFile("arrays_output.xml");
		model.writeTo(file).orElseThrow();

		// Read it back
		SdModel model2 = SdModel.readFrom(Files.newInputStream(file.toPath())).orElseThrow();

		assertEquals(model.dimensions().size(), model2.dimensions().size());
		assertEquals(model.vars().size(), model2.vars().size());
	}
}
