package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.UnitGroup;

public class ProjectVariantIOTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testInsertDelete() {
		var project = Project.of("project");
		var variant = new ProjectVariant();
		variant.name = "A project variant";
		project.variants.add(variant);
		db.insert(project);
		Assert.assertTrue(variant.id > 0L);

		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		variant.unit = units.referenceUnit;
		db.update(project);

		db.clearCache();
		var clone = db.get(Project.class, project.id);
		Assert.assertEquals(
			units.referenceUnit,
			clone.variants.get(0).unit);

		db.delete(project, units);
	}
}
