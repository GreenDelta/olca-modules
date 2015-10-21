model = 'Currency'
dao = 'CurrencyDao'
var = 'currency'

t = """package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.${dao};
import org.openlca.core.model.${model};
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ${model}Test extends AbstractZipTest {

    @Test
    public void test${model}() throws Exception {
        ${dao} dao = new ${dao}(Tests.getDb());
        ${model} $var = createModel(dao);
        doExport($var, dao);
        doImport(dao, $var);
        dao.delete($var);
    }

    private ${model} createModel(${dao} dao) {
        ${model} ${var} = new ${model}();
        ${var}.setName("$var");
        ${var}.setRefId(UUID.randomUUID().toString());
        dao.insert(${var});
        return $var;
    }

    private void doExport(${model} ${var}, ${dao} dao) {
        with(zip -> {
            JsonExport export = new JsonExport(Tests.getDb(), zip);
            export.write(${var});
        });
        dao.delete(${var});
        Assert.assertFalse(dao.contains(${var}.getRefId()));
    }

    private void doImport(${dao} dao, ${model} ${var}) {
        with(zip -> {
            JsonImport jImport = new JsonImport(zip, Tests.getDb());
            jImport.run();
        });
        Assert.assertTrue(dao.contains(${var}.getRefId()));
        ${model} clone = dao.getForRefId(${var}.getRefId());
        Assert.assertEquals(${var}.getName(), clone.getName());
    }
}
"""

print t