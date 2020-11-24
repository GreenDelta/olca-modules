package org.openlca.core.database;

import java.nio.charset.StandardCharsets;
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

	private final MappingFileDao dao = new MappingFileDao(Tests.getDb());

	@Before
	public void setUp() throws Exception {
		var mf = new MappingFile();
		mf.name = FILE_NAME;
		mf.content = BinUtils.gzip(CONTENT.getBytes(StandardCharsets.UTF_8));
		dao.insert(mf);
	}

	@After
	public void tearDown() {
		var file = dao.getForName(FILE_NAME);
		dao.delete(file);
	}

	@Test
	public void testGetAll() {
		var all = dao.getAll();
		boolean found = false;
		for (MappingFile file : all) {
			if (FILE_NAME.equals(file.name)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
	}

	@Test
	public void getNames() {
		var names = dao.getNames();
		Assert.assertTrue(names.contains(FILE_NAME));
	}

	@Test
	public void testGetForFileName() {
		MappingFile file = dao.getForName(FILE_NAME);
		Assert.assertEquals(FILE_NAME, file.name);
	}

	@Test
	public void testGetContent() throws Exception {
		MappingFile file = dao.getForName(FILE_NAME);
		String t = new String(
				BinUtils.gunzip(file.content),
				StandardCharsets.UTF_8);
		Assert.assertEquals(t, CONTENT);
	}

}
