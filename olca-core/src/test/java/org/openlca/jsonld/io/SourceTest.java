package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class SourceTest extends AbstractZipTest {

	@Test
	public void testSource() {
		SourceDao dao = new SourceDao(Tests.getDb());
		Source source = createModel(dao);
		doExport(source, dao);
		doImport(dao, source);
		dao.delete(source);
	}

	private Source createModel(SourceDao dao) {
		Source source = new Source();
		source.name = "Source";
		source.refId = UUID.randomUUID().toString();
		dao.insert(source);
		return source;
	}

	private void doExport(Source source, SourceDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(source);
		});
		dao.delete(source);
		Assert.assertFalse(dao.contains(source.refId));
	}

	private void doImport(SourceDao dao, Source source) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(source.refId));
		Source clone = dao.getForRefId(source.refId);
		Assert.assertEquals(source.name, clone.name);
	}
}
