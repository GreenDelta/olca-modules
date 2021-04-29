package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;

public class ProjectVariantIOTest {

    private final IDatabase db = Tests.getDb();
    private final ProjectVariantDao dao = new ProjectVariantDao(db);

    @Test
    public void testInsertDelete() {
        ProjectVariant variant = new ProjectVariant();
        dao.insert(variant);
        Assert.assertTrue(variant.id > 0L);
        dao.delete(variant);
    }

    @Test
    public void testUpdate() {
        Unit unit = new Unit();
        unit.name = "kg";
        UnitDao unitDao = new UnitDao(db);
        unit = unitDao.insert(unit);
        ProjectVariant variant = new ProjectVariant();
        variant = dao.insert(variant);
        variant.unit = unit;
        variant = dao.update(variant);
        Tests.emptyCache();
        variant = dao.getForId(variant.id);
        Assert.assertEquals(unit, variant.unit);
        dao.delete(variant);
        unitDao.delete(unit);
    }

}
