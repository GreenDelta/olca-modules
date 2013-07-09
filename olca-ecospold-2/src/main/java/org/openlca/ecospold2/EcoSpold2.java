package org.openlca.ecospold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class EcoSpold2 {

	private EcoSpold2() {
	}

	public static DataSet read(InputStream is) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		return DataSet.fromXml(doc);
	}

	public static DataSet read(File file) throws Exception {
		try (FileInputStream in = new FileInputStream(file)) {
			return read(in);
		}
	}

	public static void write(DataSet dataSet, File file) throws Exception {
		try (OutputStream out = new FileOutputStream(file)) {
			write(dataSet, out);
		}
	}

	public static void write(DataSet dataSet, OutputStream out)
			throws Exception {
		Document doc = dataSet.toXml();
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(doc, out);
	}

}
