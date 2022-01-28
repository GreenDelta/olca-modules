package org.openlca.validation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Source;

public class ImpactMethodTest {

	private final IDatabase db = Tests.getDb();

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testMissingSource() {
		var source = db.insert(Source.of("my source"));
		var method = ImpactMethod.of("my method");
		method.source = source;
		db.insert(method);
		var impact = ImpactCategory.of("my impact");
		impact.source = source;
		db.insert(impact);
		db.delete(source);

		var validation = Validation.on(db);
		validation.run();
		var items = validation.items();

		var methodErrorFound = false;
		var impactErrorFound = false;
		for (var item : items) {
			if (item.model == null || item.isOk())
				continue;
			if (item.model.id == method.id) {
				methodErrorFound = true;
			}
			if (item.model.id == impact.id) {
				impactErrorFound = true;
			}
		}
		Assert.assertTrue(methodErrorFound);
		Assert.assertTrue(impactErrorFound);
	}

}
