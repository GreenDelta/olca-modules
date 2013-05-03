package org.openlca.ecospold2.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.openlca.ecospold2.DataSet;

public class EcoSpold2 {

	private EcoSpold2() {
	}

	public static DataSet read(InputStream is) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		InputMapper mapper = new InputMapper(doc);
		return mapper.map();
	}

	public static DataSet read(File file) throws Exception {
		return read(new FileInputStream(file));
	}

	public static void write(DataSet dataSet, File file) throws Exception {

	}

}
