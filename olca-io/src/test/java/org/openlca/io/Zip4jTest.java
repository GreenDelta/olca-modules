package org.openlca.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Zip4jTest {

	private static Logger log = LoggerFactory.getLogger(Zip4jTest.class);

	private static File workDir;

	@BeforeClass
	public static void setUp() throws Exception {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		workDir = new File(tempDir, "zip4j_tests_"
				+ UUID.randomUUID().toString());
		workDir.mkdirs();
		log.trace("work dir created: {}", workDir);
	}

	@AfterClass
	public static void tearDown() {
		for (File f : workDir.listFiles()) {
			boolean d = f.delete();
			if (!d)
				log.warn("could not delete file {}", f);
		}
		boolean d = workDir.delete();
		if (!d)
			log.warn("could working directory {}", workDir);
	}

	@Test
	public void testAddGetStream() throws Exception {

		// write stream
		File file = new File(workDir, "zip-stream.zip");
		ZipFile zipFile = new ZipFile(file.getAbsoluteFile());
		String metaInf = "database_name: dbx\nversion: 1.4";
		byte[] bytes = metaInf.getBytes("utf-8");
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ZipParameters parameters = new ZipParameters();
		parameters.setFileNameInZip("META-INF/MANIFEST.mf");
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
		parameters.setSourceExternalStream(true);
		zipFile.addStream(in, parameters);

		// read stream
		zipFile = new ZipFile(file);
		FileHeader header = zipFile.getFileHeader("META-INF/MANIFEST.mf");
		byte[] out = new byte[bytes.length];
		zipFile.getInputStream(header).read(out);

		Assert.assertArrayEquals(bytes, out);
	}

}
