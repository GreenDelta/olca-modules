package org.openlca.geo.parameter;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.Tests;
import org.openlca.geo.parameter.ShapeFileRepository;
import org.openlca.geo.parameter.ShapeFileUtils;

import java.io.File;
import java.util.List;

public class ShapeFileUtilsTest {

	private ShapeFileRepository repository = Tests.getRepository();

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
		File shapeFile = new File(repository.getFolder(), "states.shp");
		List<File> files = ShapeFileUtils.getAllFiles(shapeFile);
		Assert.assertTrue(files.size() >= 7);
	}

	@Test
	public void testIsValid() {
		File shapeFile = new File(repository.getFolder(), "states.shp");
		Assert.assertTrue(ShapeFileUtils.isValid(shapeFile));
		shapeFile = new File(repository.getFolder(), "states_not_exists.shp");
		Assert.assertFalse(ShapeFileUtils.isValid(shapeFile));
	}

}
