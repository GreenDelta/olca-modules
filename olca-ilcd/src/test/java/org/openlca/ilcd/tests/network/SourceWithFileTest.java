package org.openlca.ilcd.tests.network;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.SampleSource;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;

public class SourceWithFileTest {

	private SodaClient client;

	@Before
	public void setUp() throws Exception {
		if (!Network.isAppAlive())
			return;
		client = Network.createClient();
	}

	@Test
	public void testSimpleSourceUpload() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		Source source = makeSource(id);
		client.put(source);
		Source fromServer = client.get(Source.class, id);
		Assert.assertEquals(id, fromServer.sourceInfo.dataSetInfo.uuid);
	}

	@Test
	public void testSourceWithFileUpload() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		String id = UUID.randomUUID().toString();
		Source source = makeSource(id);
		Path tempFile = Files.createTempFile("soda_upload_test", ".txt");
		byte[] content = "Test file content".getBytes();
		Files.write(tempFile, content);
		File file = tempFile.toFile();
		addFileLink(source, file);
		client.put(source, new File[] { file });
		InputStream is = client.getExternalDocument(id, file.getName());
		byte[] contentFromServer = new byte[content.length];
		is.read(contentFromServer);
		is.close();
		Assert.assertArrayEquals(content, contentFromServer);
	}

	@Test(expected = DataStoreException.class)
	public void testNoFile() throws Exception {
		Assume.assumeTrue(Network.isAppAlive());
		client.getExternalDocument(UUID.randomUUID().toString(),
				"no_such_file.txt");
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
