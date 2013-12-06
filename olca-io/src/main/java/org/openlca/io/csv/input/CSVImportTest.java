package org.openlca.io.csv.input;

import java.io.File;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.mapping.MapFormat;
import org.openlca.core.model.mapping.Mapping;
import org.openlca.core.model.mapping.content.ES1FlowContent;
import org.openlca.core.model.mapping.content.MappingContent;

public class CSVImportTest {

	public static void main(String[] args) throws Exception {
		IDatabase database = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/csvTestImport", "root", "");

//		Mapping mapping = new Mapping();
//		mapping.setInput(true);
//		mapping.setModelType(ModelType.FLOW);
//		mapping.setFormat(MapFormat.CSV);
//
//		ES1FlowContent es1FlowContent = new ES1FlowContent();
//		es1FlowContent.setContent("content");
//		ES1FlowContent es1FlowContent2 = new ES1FlowContent();
//		es1FlowContent2.setContent("olca content");
//		mapping.setContent(es1FlowContent);
//		mapping.setOlca_content(es1FlowContent2);
//
//		BaseDao<MappingContent> contentDao = new BaseDao<>(
//				MappingContent.class, database);
//		contentDao.insert(es1FlowContent);
//		contentDao.insert(es1FlowContent2);
//
//		BaseDao<Mapping> mappingDao = new BaseDao<>(Mapping.class, database);
//		mappingDao.insert(mapping);

		// File file = new File("/Users/imo/Desktop/testProcess.csv");
		// CSVImporter importer = new CSVImporter(file, database);
		// importer.run();
		database.close();
	}
}
