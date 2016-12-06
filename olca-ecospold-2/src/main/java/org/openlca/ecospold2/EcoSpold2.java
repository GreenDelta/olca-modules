package org.openlca.ecospold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

@XmlRootElement(name = "ecoSpold", namespace = "http://www.EcoInvent.org/EcoSpold02")
public class EcoSpold2 {

	@XmlElement(name = "activityDataset")
	public DataSet dataSet;

	@XmlElement(name = "childActivityDataset")
	public DataSet childDataSet;

	/** Reads an activity data set from an EcoSpold 02 file. */
	public static DataSet readDataSet(InputStream is) throws Exception {
		return DataSet.fromXml(readDoc(is));
	}

	/** Reads an activity data set from an EcoSpold 02 file. */
	public static DataSet readDataSet(File file) throws Exception {
		return DataSet.fromXml(readDoc(file));
	}

	/** Reads a list of persons from a master-data file with persons. */
	public static PersonList readPersons(InputStream is) throws Exception {
		return PersonList.fromXml(readDoc(is));
	}

	/** Reads a list of persons from a master-data file with persons. */
	public static PersonList readPersons(File file) throws Exception {
		return PersonList.fromXml(readDoc(file));
	}

	/** Reads a list of sources from a master-data file with sources. */
	public static SourceList readSources(InputStream is) throws Exception {
		return SourceList.fromXml(readDoc(is));
	}

	/** Reads a list of sources from a master-data file with sources. */
	public static SourceList readSources(File file) throws Exception {
		return SourceList.fromXml(readDoc(file));
	}

	private static Document readDoc(File file) throws Exception {
		try (FileInputStream in = new FileInputStream(file)) {
			return readDoc(in);
		}
	}

	private static Document readDoc(InputStream is) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		return doc;
	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void writeDataSet(DataSet dataSet, File file)
			throws Exception {
		writeDoc(file, dataSet.toXml());
	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void writeDataSet(DataSet dataSet, OutputStream out)
			throws Exception {
		writeDoc(out, dataSet.toXml());
	}

	/** Writes a person list to an EcoSpold 02 master data file. */
	public static void writePersons(PersonList list, File file)
			throws Exception {
		writeDoc(file, list.toXml());
	}

	/** Writes a person list to an EcoSpold 02 master data file. */
	public static void writePersons(PersonList list, OutputStream out)
			throws Exception {
		writeDoc(out, list.toXml());
	}

	/** Writes a source list to an EcoSpold 02 master data file. */
	public static void writeSources(SourceList list, File file)
			throws Exception {
		writeDoc(file, list.toXml());
	}

	/** Writes a source list to an EcoSpold 02 master data file. */
	public static void writeSources(SourceList list, OutputStream out)
			throws Exception {
		writeDoc(out, list.toXml());
	}

	private static void writeDoc(File file, Document doc) throws Exception {
		try (OutputStream out = new FileOutputStream(file)) {
			writeDoc(out, doc);
		}
	}

	private static void writeDoc(OutputStream out, Document doc)
			throws Exception {
		XMLOutputter outputter = new XMLOutputter();
		outputter.output(doc, out);
	}

}
