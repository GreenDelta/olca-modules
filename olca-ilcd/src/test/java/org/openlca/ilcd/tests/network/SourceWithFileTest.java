package org.openlca.ilcd.tests.network;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.ilcd.sources.DataSetInformation;
import org.openlca.ilcd.sources.DigitalFileReference;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.SourceBuilder;

public class SourceWithFileTest {

	private NetworkClient client;

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
		client.put(source, id);
		Source fromServer = client.get(Source.class, id);
		Assert.assertEquals(id, fromServer.getSourceInformation()
				.getDataSetInformation().getUUID());
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
		client.put(source, id, file);
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
		DataSetInformation info = new DataSetInformation();
		String name = "xtest source - " + new Random().nextInt(1000);
		LangString.addLabel(info.getShortName(), name);
		info.setUUID(id);
		Source source = SourceBuilder
				.makeSource()
				.withDataSetInfo(info)
				.withBaseUri(Network.RESOURCE_URL).getSource();
		return source;
	}

	private void addFileLink(Source source, File file) {
		DigitalFileReference ref = new DigitalFileReference();
		ref.setUri("../external_docs/" + file.getName());
		source.getSourceInformation()
				.getDataSetInformation()
				.getReferenceToDigitalFile()
				.add(ref);
	}
}
