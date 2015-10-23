package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ProcessTest extends AbstractZipTest {

	@Test
	public void testProcess() throws Exception {
		ProcessDao dao = new ProcessDao(Tests.getDb());
		Process process = createModel(dao);
		doExport(process, dao);
		doImport(dao, process);
		dao.delete(process);
	}

	private Process createModel(ProcessDao dao) {
		Process process = new Process();
		process.setName("process");
		process.setRefId(UUID.randomUUID().toString());
		dao.insert(process);
		return process;
	}

	private void doExport(Process process, ProcessDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(process);
		});
		dao.delete(process);
		Assert.assertFalse(dao.contains(process.getRefId()));
	}

	private void doImport(ProcessDao dao, Process process) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(process.getRefId()));
		Process clone = dao.getForRefId(process.getRefId());
		Assert.assertEquals(process.getName(), clone.getName());
	}
}
