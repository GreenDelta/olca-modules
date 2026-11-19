package org.openlca.io.ecospold1.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ES1UnitFetchTest {

	private File dir;

	@Before
	public void setup() throws IOException {
		dir = Files.createTempDirectory("_olca_es1_units").toFile();
	}

	@After
	public void cleanup() throws IOException {
		var files = dir.listFiles();
		if (files != null) {
			for (var f : files) {
				Files.delete(f.toPath());
			}
		}
		Files.delete(dir.toPath());
	}

	@Test
	public void testSkipUnreadableZipEntries() throws IOException {
		var zip = new File(dir, "data.ZIP");
		try (var out = new ZipOutputStream(new FileOutputStream(zip))) {
			put(out, "example.xml", example());
			// AppleDouble file as created by macOS
			put(out, "__MACOSX/._example.xml", new byte[]{0, 5, 22, 7, 0, 2, 0, 0});
			put(out, "broken.XML", bytes("this is not XML"));
		}

		var result = ES1UnitFetch.collectFrom(new File[]{zip}).value();
		assertEquals(List.of("kg"), result.units());

		var errors = result.parserErrors();
		assertEquals(2, errors.size());
		var path = zip.getPath();
		assertTrue(errors.get(0).startsWith(path + "/__MACOSX/._example.xml: "));
		assertTrue(errors.get(1).startsWith(path + "/broken.XML: "));
	}

	@Test
	public void testSkipUnreadableXmlFile() throws IOException {
		var good = new File(dir, "example.xml");
		Files.write(good.toPath(), example());
		var bad = new File(dir, "broken.xml");
		Files.write(bad.toPath(), bytes("this is not XML"));

		var result = ES1UnitFetch.collectFrom(new File[]{bad, good}).value();
		assertEquals(List.of("kg"), result.units());
		assertTrue(result.hasParserErrors());
		assertEquals(1, result.parserErrors().size());
		assertTrue(result.parserErrors().getFirst().startsWith(bad.getPath() + ": "));
	}

	@Test
	public void testSkipRefOfImpacts() throws IOException {
		// an impact method with a namespace prefix, where the element names
		// differ from the local names
		var xml = """
			<es:ecoSpold xmlns:es="http://www.EcoInvent.org/EcoSpold01Impact">
			  <es:dataset>
			    <es:metaInformation>
			      <es:processInformation>
			        <es:referenceFunction unit="kg CO2-Eq"/>
			      </es:processInformation>
			    </es:metaInformation>
			    <es:flowData>
			      <es:exchange unit="m3"/>
			    </es:flowData>
			  </es:dataset>
			</es:ecoSpold>
			""";
		var impact = new File(dir, "impact.xml");
		Files.write(impact.toPath(), bytes(xml));
		var process = new File(dir, "example.xml");
		Files.write(process.toPath(), example());

		var result = ES1UnitFetch.collectFrom(new File[]{impact, process}).value();
		assertEquals(List.of("kg", "m3"), result.units());
		assertFalse(result.hasParserErrors());
	}

	@Test
	public void testErrorForUnreadableFile() {
		var missing = new File(dir, "missing.zip");
		var res = ES1UnitFetch.collectFrom(new File[]{missing});
		assertTrue(res.isError());
		assertTrue(res.error().contains(missing.getPath()));

		assertTrue(ES1UnitFetch.collectFrom(null).isError());
	}

	private byte[] example() throws IOException {
		try (var in = getClass().getResourceAsStream("../example.xml")) {
			Assert.assertNotNull(in);
			return in.readAllBytes();
		}
	}

	private byte[] bytes(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	private void put(ZipOutputStream out, String name, byte[] data)
		throws IOException {
		out.putNextEntry(new ZipEntry(name));
		out.write(data);
		out.closeEntry();
	}
}
