package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Mapping;

import java.util.List;

public class MappingDaoTest {

	private MappingDao dao = new MappingDao(TestSession.getDefaultDatabase());

	@Test
	public void testGetAll () {
		Mapping mapping = getMapping();
		List<Mapping> all = dao.getAll();
		Assert.assertTrue(all.contains(mapping));
		dao.delete(mapping);
	}

	@Test
	public void testGetForExports() {
		Mapping mapping = getMapping();
		List<Mapping> exportMappings = dao.getAllForExport("ECO_SPOLD");
		Assert.assertFalse(exportMappings.contains(mapping));
		dao.delete(mapping);
	}

	@Test
	public void testGetForImports() {
		Mapping mapping = getMapping();
		List<Mapping> importMappings = dao.getAllForImport("ECO_SPOLD");
		Assert.assertTrue(importMappings.contains(mapping));
		dao.delete(mapping);
	}

	private Mapping getMapping() {
		Mapping mapping = new Mapping();
		mapping.setExternalContent("{'name':'kg','uuid':'757..55'}");
		mapping.setOlcaContent("{'name':'kg','uuid':'111..111'}");
		mapping.setForImport(true);
		mapping.setMappingType("ECO_SPOLD");
		mapping = dao.insert(mapping);
		TestSession.emptyCache();
		return mapping;
	}

}
