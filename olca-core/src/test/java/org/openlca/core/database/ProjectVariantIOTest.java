package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.Unit;

public class ProjectVariantIOTest {

    private IDatabase db = Tests.getDb();
    private ProjectVariantDao dao = new ProjectVariantDao(db);

    @Test
    public void testInsertDelete() {
        ProjectVariant variant = new ProjectVariant();
        dao.insert(variant);
        Assert.assertTrue(variant.getId() > 0L);
        dao.delete(variant);
    }

    @Test
    public void testUpdate() {
        Unit unit = new Unit();
        unit.setName("kg");
        UnitDao unitDao = new UnitDao(db);
        unit = unitDao.insert(unit);
        ProjectVariant variant = new ProjectVariant();
        variant = dao.insert(variant);
        variant.setUnit(unit);
        variant = dao.update(variant);
        Tests.emptyCache();
        variant = dao.getForId(variant.getId());
        Assert.assertEquals(unit, variant.getUnit());
        dao.delete(variant);
        unitDao.delete(unit);
    }

}
