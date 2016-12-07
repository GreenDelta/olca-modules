package org.openlca.ecospold2.master;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SourceList {

	public final List<Source> sources = new ArrayList<>();

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

	/** Reads a list of sources from a master-data file with sources. */
	public static SourceList readSources(InputStream is) throws Exception {
		return SourceList.fromXml(readDoc(is));
	}

	/** Reads a list of sources from a master-data file with sources. */
	public static SourceList readSources(File file) throws Exception {
		return SourceList.fromXml(readDoc(file));
	}
}
