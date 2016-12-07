package org.openlca.ecospold2.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "validSources")
public class SourceList {

	@XmlElement(name = "source")
	public final List<Source> sources = new ArrayList<>();

	public static SourceList read(File file) {
		try (FileInputStream stream = new FileInputStream(file)) {
			return read(stream);
		} catch (Exception e) {
			String m = "failed to read source master data: " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static SourceList read(InputStream is) {
		try {
			return JAXB.unmarshal(is, SourceList.class);
		} catch (Exception e) {
			String m = "failed to read source master data";
			throw new RuntimeException(m, e);
		}
	}

	public static void write(SourceList list, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(list, fos);
		} catch (Exception e) {
			String m = "failed to write source master data:  " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static void write(SourceList list, OutputStream out) {
		try {
			JAXB.marshal(list, out);
		} catch (Exception e) {
			String m = "failed to write source master data";
			throw new RuntimeException(m, e);
		}
	}

}
