package spold2;

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
@XmlRootElement(name = "validPersons")
public class PersonList {

	@XmlElement(name = "person")
	public final List<Person> persons = new ArrayList<>();

	public static PersonList read(File file) {
		try (FileInputStream stream = new FileInputStream(file)) {
			return read(stream);
		} catch (Exception e) {
			String m = "failed to read person master data: " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static PersonList read(InputStream is) {
		try {
			return JAXB.unmarshal(is, PersonList.class);
		} catch (Exception e) {
			String m = "failed to read person master data";
			throw new RuntimeException(m, e);
		}
	}

	public static void write(PersonList list, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(list, fos);
		} catch (Exception e) {
			String m = "failed to write person master data:  " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static void write(PersonList list, OutputStream out) {
		try {
			JAXB.marshal(list, out);
		} catch (Exception e) {
			String m = "failed to write person master data";
			throw new RuntimeException(m, e);
		}
	}

}
