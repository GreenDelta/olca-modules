package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.MappingFile;
import org.openlca.util.BinUtils;

public class MappingDaoTest {

	private final String FILE_NAME = "units_test_" + UUID.randomUUID() + ".csv";
	private final String CONTENT = "kg;1.0\ng;1000\n";

	private MappingFileDao dao = new MappingFileDao(
			Tests.getDb());

	@Before
	public void setUp() throws Exception {
		MappingFile mappingFile = new MappingFile();
		mappingFile.setFileName(FILE_NAME);
		byte[] content = BinUtils.zip(CONTENT.getBytes("utf-8"));
		mappingFile.setContent(content);
		dao.insert(mappingFile);
	}

	@After
	public void tearDown() throws Exception {
		MappingFile file = dao.getForFileName(FILE_NAME);
		dao.delete(file);
	}

	@Test
	public void testGetAll() {
		List<MappingFile> all = dao.getAll();
		boolean found = false;
		for (MappingFile file : all) {
			if (FILE_NAME.equals(file.getFileName())) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

	@Test
	public void testGetForFileName() {
		MappingFile file = dao.getForFileName(FILE_NAME);
		Assert.assertEquals(FILE_NAME, file.getFileName());
	}

	@Test
	public void testGetContent() throws Exception {
		MappingFile file = dao.getForFileName(FILE_NAME);
		String t = new String(BinUtils.unzip(file.getContent()), "utf-8");
		Assert.assertEquals(t, CONTENT);
	}

}
