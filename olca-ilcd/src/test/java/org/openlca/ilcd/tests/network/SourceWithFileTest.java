package org.openlca.ilcd.tests.network;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.SampleSource;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;

public class SourceWithFileTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!TestServer.isAvailable())
			return;
		client = TestServer.newClient();
	}

	@Test
	public void testSimpleSourceUpload() {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = UUID.randomUUID().toString();
		Source source = makeSource(id);
		client.put(source);
		Source fromServer = client.get(Source.class, id);
		assertEquals(id, fromServer.sourceInfo.dataSetInfo.uuid);
	}

	@Test
	public void testSourceWithFileUpload() throws Exception {
		Assume.assumeTrue(TestServer.isAvailable());
		String id = UUID.randomUUID().toString();
		Source source = makeSource(id);
		Path tempFile = Files.createTempFile("soda_upload_test", ".txt");
		byte[] content = "Test file content".getBytes();
		Files.write(tempFile, content);
		File file = tempFile.toFile();
		addFileLink(source, file);
		client.put(source, new File[] { file });

		// try to get the file from the server
		try (var stream = client.getExternalDocument(id, file.getName())) {
			byte[] contentFromServer = new byte[content.length];
			assertEquals(content.length, stream.read(contentFromServer));
			assertArrayEquals(content, contentFromServer);
		}
	}

	@Test(expected = Exception.class)
	public void testNoFile(){
		Assume.assumeTrue(TestServer.isAvailable());
		var randomID = UUID.randomUUID().toString();
		client.getExternalDocument(randomID, "no_such_file.txt");
	}

	private Source makeSource(String id) {
		Source source = SampleSource.create();
		source.sourceInfo.dataSetInfo.uuid = id;
		return source;
	}

	private void addFileLink(Source source, File file) {
		FileRef ref = new FileRef();
		ref.uri = "../external_docs/" + file.getName();
		source.sourceInfo.dataSetInfo.files
				.add(ref);
	}
}
