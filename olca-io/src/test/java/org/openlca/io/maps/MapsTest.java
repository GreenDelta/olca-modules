package org.openlca.io.maps;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
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
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class MapsTest {

	private final String CONTENT = "\"aString\";42.42;42;\n";
	private final String FILE_NAME = "test_map_" + UUID.randomUUID() + ".csv";

	private IDatabase database = Tests.getDb();

	@Before
	public void setUp() throws Exception {
		InputStream stream = new ByteArrayInputStream(CONTENT.getBytes("utf-8"));
		Maps.store(FILE_NAME, stream, database);
	}

	@After
	public void tearDown() throws Exception {
		MappingFileDao dao = new MappingFileDao(database);
		MappingFile file = dao.getForFileName(FILE_NAME);
		dao.delete(file);
	}

	@Test
	public void testMappingFile() throws Exception {
		MappingFileDao dao = new MappingFileDao(database);
		MappingFile file = dao.getForFileName(FILE_NAME);
		String t = new String(BinUtils.unzip(file.getContent()), "utf-8");
		Assert.assertEquals(CONTENT, t);
	}

	@Test
	public void testCellReader() throws Exception {
		CellProcessor[] processors = { null, new ParseDouble(), new ParseInt(),
				new Optional() };
		List<List<Object>> results = Maps.readAll(FILE_NAME, database,
				processors);
		Assert.assertEquals(1, results.size());
		List<Object> row = results.get(0);
		Assert.assertEquals("aString", Maps.getString(row, 0));
		Assert.assertEquals(42.42, Maps.getDouble(row, 1), 1e-16);
		Assert.assertEquals(42, Maps.getInt(row, 2));
		Assert.assertNull(Maps.getString(row, 3));
	}

}
