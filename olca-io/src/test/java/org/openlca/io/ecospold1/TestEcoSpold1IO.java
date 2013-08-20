package org.openlca.io.ecospold1;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;
import org.openlca.io.TestSession;
import org.openlca.io.UnitMapping;
import org.openlca.io.ecospold1.exporter.EcoSpold01Outputter;
import org.openlca.io.ecospold1.importer.EcoSpold01Import;

public class TestEcoSpold1IO {

	private IDatabase database = TestSession.getDerbyDatabase();

	@Test
	public void testIO() throws Exception {
		InputStream in = openXml();
		DataSetType type = EcoSpoldIO.getEcoSpoldType(in);
		EcoSpold01Import es1Import = new EcoSpold01Import(database,
				UnitMapping.createDefault(database));
		es1Import.run(openXml(), type);
		ProcessDao dao = new ProcessDao(database);
		List<Process> processes = dao.getForName("Bauxite, at mine");
		Process process = processes.get(0);
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpDirPath);
		EcoSpold01Outputter outputter = new EcoSpold01Outputter(dir);
		outputter.exportProcess(process);
		dao.delete(process);
		File esDir = new File(dir, "EcoSpold01");
		File file = new File(esDir, "process_" + process.getRefId() + ".xml");
		Assert.assertTrue(file.exists());
		file.delete();
	}

	private InputStream openXml() {
		return getClass().getResourceAsStream("sample_ecospold01.xml");
	}

}
