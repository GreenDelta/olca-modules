package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.model.Mapping;
import org.openlca.core.model.ModelType;

public class MappingDaoTest {

	private MappingDao dao = new MappingDao(TestSession.getDefaultDatabase());

	@Test
	public void testGetAll() {
		Mapping mapping = getMapping();
		List<Mapping> all = dao.getAll();
		Assert.assertTrue(all.contains(mapping));
		dao.delete(mapping);
	}

	@Test
	public void testGetForExports() {
		Mapping mapping = getMapping();
		List<Mapping> exportMappings = dao.getAllForExport(ModelType.UNIT,
				"ECOSPOLD_2");
		Assert.assertFalse(exportMappings.contains(mapping));
		dao.delete(mapping);
	}

	@Test
	public void testGetForImports() {
		Mapping mapping = getMapping();
		List<Mapping> importMappings = dao.getAllForImport(ModelType.UNIT,
				"ECOSPOLD_2");
		Assert.assertTrue(importMappings.contains(mapping));
		dao.delete(mapping);
	}

	private Mapping getMapping() {
		Mapping mapping = new Mapping();
		mapping.setContent("{'name':'kg','uuid':'757..55'}");
		mapping.setOlcaRefId(UUID.randomUUID().toString());
		mapping.setForImport(true);
		mapping.setFormat("ECOSPOLD_2");
		mapping.setModelType(ModelType.UNIT);
		mapping = dao.insert(mapping);
		TestSession.emptyCache();
		return mapping;
	}

}
