package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.model.SocialIndicator;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class SocialIndicatorTest extends AbstractZipTest {

	@Test
	public void testSocialIndicator() {
		SocialIndicatorDao dao = new SocialIndicatorDao(Tests.getDb());
		SocialIndicator indicator = createModel(dao);
		doExport(indicator, dao);
		doImport(dao, indicator);
		dao.delete(indicator);
	}

	private SocialIndicator createModel(SocialIndicatorDao dao) {
		SocialIndicator indicator = new SocialIndicator();
		indicator.name = "indicator";
		indicator.refId = UUID.randomUUID().toString();
		dao.insert(indicator);
		return indicator;
	}

	private void doExport(SocialIndicator indicator, SocialIndicatorDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(indicator);
		});
		dao.delete(indicator);
		Assert.assertFalse(dao.contains(indicator.refId));
	}

	private void doImport(SocialIndicatorDao dao, SocialIndicator indicator) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(indicator.refId));
		SocialIndicator clone = dao.getForRefId(indicator.refId);
		Assert.assertEquals(indicator.name, clone.name);
	}
}
