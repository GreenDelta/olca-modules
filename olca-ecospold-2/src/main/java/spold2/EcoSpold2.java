package spold2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ecoSpold")
public class EcoSpold2 {

	@XmlElement(name = "activityDataset")
	public DataSet dataSet;

	@XmlElement(name = "childActivityDataset")
	public DataSet childDataSet;

	/** Reads an activity data set from an EcoSpold 02 file. */
	public static DataSet read(File file) {
		try (FileInputStream stream = new FileInputStream(file)) {
			return read(stream);
		} catch (Exception e) {
			String m = "failed to read EcoSpold 2 file: " + file;
			throw new RuntimeException(m, e);
		}
	}

	/** Reads an activity data set from an EcoSpold 02 file. */
	public static DataSet read(InputStream is) {
		try {
			EcoSpold2 spold = JAXB.unmarshal(is, EcoSpold2.class);
			return spold.dataSet != null ? spold.dataSet : spold.childDataSet;
		} catch (Exception e) {
			String m = "failed to read EcoSpold 2 document";
			throw new RuntimeException(m, e);
		}
	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void write(DataSet dataSet, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(dataSet, fos);
		} catch (Exception e) {
			String m = "failed to write data set to file: " + file;
			throw new RuntimeException(m, e);
		}

	}

	/** Writes an activity data set to an EcoSpold 02 file. */
	public static void write(DataSet dataSet, OutputStream out) {
		try {
			EcoSpold2 spold2 = new EcoSpold2();
			spold2.dataSet = dataSet;
			JAXB.marshal(spold2, out);
		} catch (Exception e) {
			String m = "failed to write data set";
			throw new RuntimeException(m, e);
		}
	}

}
