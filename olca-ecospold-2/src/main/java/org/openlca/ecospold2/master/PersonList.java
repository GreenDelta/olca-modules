package org.openlca.ecospold2.master;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a list of persons in a master data file.
 */
public class PersonList {

	public final List<Person> persons = new ArrayList<>();

	/** Reads a list of persons from a master-data file with persons. */
	public static PersonList readPersons(InputStream is) throws Exception {
		return PersonList.fromXml(readDoc(is));
	}

	/** Reads a list of persons from a master-data file with persons. */
	public static PersonList readPersons(File file) throws Exception {
		return PersonList.fromXml(readDoc(file));
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

}
