package org.openlca.geo.parameter;

import java.io.File;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openlca.geo.Tests;
import org.openlca.geo.parameter.ShapeFileParameter;
import org.openlca.geo.parameter.ShapeFileRepository;

public class ShapeFileRepositoryTest {

	private ShapeFileRepository repository = Tests.getRepository();
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testGetShapeFiles() {
		List<String> shapeFiles = repository.getShapeFiles();
		Assert.assertEquals(1, shapeFiles.size());
		Assert.assertEquals("states", shapeFiles.get(0));
	}

	@Test
	public void testOpenDataStore() throws Exception {
		DataStore dataStore = repository.openDataStore("states");
		String typeName = dataStore.getTypeNames()[0];
		Assert.assertEquals("states", typeName);
		SimpleFeatureCollection source = dataStore.getFeatureSource(typeName)
				.getFeatures();
		Assert.assertEquals(51, source.size());
		dataStore.dispose();
	}

	@Test
	public void testGetParameters() {
		List<ShapeFileParameter> parameters = repository
				.getParameters("states");
		Assert.assertEquals(1, parameters.size());
		ShapeFileParameter parameter = parameters.get(0);
		Assert.assertEquals("DRAWSEQ", parameter.getName());
		Assert.assertEquals(1, parameter.getMin(), 1e-17);
		Assert.assertEquals(51, parameter.getMax(), 1e-17);
	}

	@Test
	public void testImportFile() {
		ShapeFileRepository tempRepository = new ShapeFileRepository(
				tempFolder.getRoot());
		Assert.assertTrue(tempRepository.getShapeFiles().isEmpty());
		File shapeFile = new File(repository.getFolder(), "states.shp");
		tempRepository.importFile(shapeFile);
		Assert.assertTrue(tempRepository.getShapeFiles().size() == 1);
	}

}
