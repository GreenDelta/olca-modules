package spold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXB;

public class IO {

	public static <T> T read(File file, Class<T> type) {
		try (FileInputStream stream = new FileInputStream(file)) {
			return read(stream, type);
		} catch (Exception e) {
			String m = "failed to read file " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static <T> T read(InputStream is, Class<T> type) {
		try {
			return JAXB.unmarshal(is, type);
		} catch (Exception e) {
			String m = "failed to read stream";
			throw new RuntimeException(m, e);
		}
	}

	public static void write(Object obj, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(obj, fos);
		} catch (Exception e) {
			String m = "failed to write file: " + file;
			throw new RuntimeException(m, e);
		}
	}

	public static void write(Object obj, OutputStream out) {
		try {
			JAXB.marshal(obj, out);
		} catch (Exception e) {
			String m = "failed to write data";
			throw new RuntimeException(m, e);
		}
	}

}
