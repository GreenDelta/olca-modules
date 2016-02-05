package org.openlca.geo.parameter;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.Tests;

public class ShapeFileUtilsTest {

	private ShapeFileFolder repository = Tests.getRepository();

	@Test
	public void testGetName() {
		File file = new File("test.shp");
		Assert.assertEquals("test", ShapeFileUtils.getName(file));
		file = new File("test.shp.xml");
		Assert.assertEquals("test", ShapeFileUtils.getName(file));
		Assert.assertNull(ShapeFileUtils.getName(null));
	}

	@Test
	public void testGetAllFiles() {
		File shapeFile = new File(repository.folder, "states.shp");
		List<File> files = ShapeFileUtils.getAllFiles(shapeFile);
		Assert.assertTrue(files.size() >= 7);
	}

	@Test
	public void testIsValid() {
		File shapeFile = new File(repository.folder, "states.shp");
		Assert.assertTrue(ShapeFileUtils.isValid(shapeFile));
		shapeFile = new File(repository.folder, "states_not_exists.shp");
		Assert.assertFalse(ShapeFileUtils.isValid(shapeFile));
	}

}
