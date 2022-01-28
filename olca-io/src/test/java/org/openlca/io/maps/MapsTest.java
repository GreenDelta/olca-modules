package org.openlca.io.maps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.io.Tests;
import org.openlca.util.BinUtils;

public class MapsTest {

	private final String CONTENT = "\"aString\";42.42;42;\n";
	private final String FILE_NAME = "test_map_" + UUID.randomUUID() + ".csv";

	private final IDatabase database = Tests.getDb();

	@Before
	public void setUp() {
		InputStream stream = new ByteArrayInputStream(
				CONTENT.getBytes(StandardCharsets.UTF_8));
		Maps.store(FILE_NAME, stream, database);
	}

	@After
	public void tearDown() {
		MappingFileDao dao = new MappingFileDao(database);
		MappingFile file = dao.getForName(FILE_NAME);
		dao.delete(file);
	}

	@Test
	public void testMappingFile() throws Exception {
		var dao = new MappingFileDao(database);
		var file = dao.getForName(FILE_NAME);
		var t = new String(BinUtils.gunzip(file.content), StandardCharsets.UTF_8);
		Assert.assertEquals(CONTENT, t);
	}

}
