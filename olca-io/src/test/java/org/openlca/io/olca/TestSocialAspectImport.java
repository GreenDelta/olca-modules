package org.openlca.io.olca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.Derby;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;

public class TestSocialAspectImport {

	/**
	 * Test that social aspects of processes are copied between databases:
	 * https://github.com/GreenDelta/olca-app/issues/88. We ignore this test by
	 * default, because creating temporary databases take so long...
	 */
	@Test
	@Ignore
	public void testCopySocialAspects() throws Exception {
		IDatabase db1 = Derby.createInMemory();
		SocialIndicator indicator = new SocialIndicator();
		indicator.refId = "si";
		SocialIndicatorDao idao = new SocialIndicatorDao(db1);
		idao.insert(indicator);

		Process process = new Process();
		process.refId = "pr";
		SocialAspect aspect = new SocialAspect();
		aspect.indicator = indicator;
		process.socialAspects.add(aspect);
		ProcessDao pdao = new ProcessDao(db1);
		pdao.insert(process);

		IDatabase db2 = Derby.createInMemory();
		DatabaseImport imp = new DatabaseImport(db1, db2);
		imp.run();
		db1.close();

		idao = new SocialIndicatorDao(db2);
		indicator = idao.getForRefId("si");
		assertNotNull(indicator);

		pdao = new ProcessDao(db2);
		process = pdao.getForRefId("pr");
		assertEquals(1, process.socialAspects.size());
		aspect = process.socialAspects.get(0);
		assertNotNull(aspect.indicator);
		assertEquals("si", aspect.indicator.refId);
		db2.close();
	}

}
